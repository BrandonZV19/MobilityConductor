package com.softtim.mobilityconductor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Created by softtim on 8/22/17.
 */

public class BlueService extends Service implements MiAsyncTask.MiCallback {
    private String NOMBRE_DISPOSITIVO_BT;//Nombre de neustro dispositivo bluetooth.
    private MiAsyncTask tareaAsincrona;
    int counter=0,idUnidad;
    SharedPreferences preferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("BLUE","created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);
        idUnidad = preferences.getInt(getString(R.string.p_id_unidad), 0);
        if (idUnidad==0){
            Intent intents = new Intent(getBaseContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intents.addCategory("FMS");
            intents.putExtra("whatToDo", "salir");
            getApplication().startActivity(intents);
            Toast.makeText(BlueService.this, "Datos de sesion invalidos.", Toast.LENGTH_LONG).show();
            stopSelf();
        }else {
            ///NOMBRE_DISPOSITIVO_BT="Unidad "+idUnidad;
            NOMBRE_DISPOSITIVO_BT="HC-06";
            descubrirDispositivosBT();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tareaAsincrona != null) {
            tareaAsincrona.cancel(true);
        }
    }

    private void descubrirDispositivosBT() {
        Log.e("BLUE","in 1");
/*
Este método comprueba si nuestro dispositivo dispone de conectividad bluetooh.
En caso afirmativo, si estuviera desctivada, intenta activarla.
En caso negativo presenta un mensaje al usuario y sale de la aplicación.
*/
//Comprobamos que el dispositivo tiene adaptador bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null) {

//El dispositivo tiene adapatador BT. Ahora comprobamos que bt esta activado.

            if (mBluetoothAdapter.isEnabled()) {
//Esta activado. Obtenemos la lista de dispositivos BT emparejados con nuestro dispositivo android.

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//Si hay dispositivos emparejados
                if (pairedDevices.size() > 0) {
/*
Recorremos los dispositivos emparejados hasta encontrar el
adaptador BT del arduino, en este caso se llama HC-06
*/

                    BluetoothDevice arduino = null;

                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equalsIgnoreCase(NOMBRE_DISPOSITIVO_BT)) {
                            arduino = device;
                        }
                    }

                    if (arduino != null) {
                        Log.e("BLUE","arduino="+arduino.toString());
                        Toast.makeText(BlueService.this, "Boton de emergencia emparejado, asegurate que este conectado.", Toast.LENGTH_LONG).show();
                        tareaAsincrona = new MiAsyncTask(BlueService.this);
                        tareaAsincrona.execute(arduino);
                    } else {
//No hemos encontrado nuestro dispositivo BT, es necesario emparejarlo antes de poder usarlo.
//No hay ningun dispositivo emparejado. Salimos de la app.
                        Toast.makeText(BlueService.this, "No hay dispositivos emparejados, por favor empareje el boton de emergencia.", Toast.LENGTH_LONG).show();
                        Log.e("BLUE","no 1");
                    }
                } else {
//No hay ningun dispositivo emparejado. Salimos de la app.
                    Toast.makeText(BlueService.this, "No hay dispositivos emparejados, por favor empareje el boton de emergencia.", Toast.LENGTH_LONG).show();
                    Log.e("BLUE","no 2");
                }
            } else {
                Toast.makeText(BlueService.this, "Bluetooth desactivado, por favor empareje el boton de emergencia.", Toast.LENGTH_LONG).show();
                Log.e("BLUE","no 3");
            }
        } else {
// El dispositivo no soporta bluetooth. Mensaje al usuario y salimos de la app
            Toast.makeText(BlueService.this, "El dispositivo no soporta comunicación por Bluetooth", Toast.LENGTH_LONG).show();
            Log.e("BLUE","no 4");
            stopSelf();
        }
    }

    @Override
    public void onTaskCompleted() {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onTemperaturaUpdate(Temperatura p) {
        if (p.getTemperatura().contains("on")){
            Log.e("BLUE","on");
            if (counter<1) {
                Log.e("BLUE","count 0");
                counter=counter+1;
                Intent intent = new Intent(getBaseContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory("FMS");
                intent.putExtra("whatToDo", "alerta");
                getApplication().startActivity(intent);
                Toast.makeText(BlueService.this, "Alerta enviada correctamente. Boton desconectado", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        }
    }
}
