package com.brandonzamudio.softtim.mobilityconductor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    Button iniciar;
    EditText user,pass;
    SharedPreferences preferences;
    String LOG_TAG="LoginActivity";
    ProgressDialog progressDialog;
    AlertDialog dialogCampoVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.logo);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        preferences=getApplicationContext().getSharedPreferences(String.valueOf(R.string.preferences),MODE_PRIVATE);

        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(String.valueOf(R.string.accediendo));

        user=(EditText)findViewById(R.id.loginUser);
        pass=(EditText)findViewById(R.id.loginPass);

        iniciar=(Button)findViewById(R.id.loginGo);
        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getText().toString().trim().length()>0
                        && pass.getText().toString().trim().length()>0){
                    login();
                }else {
                    dialogCampoVacio=null; //Antes de crear una nueva vista del dialog lo ponemos como null
                    dialogCampoVacio=new AlertDialog.Builder(LoginActivity.this)
                            .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setMessage(getString(R.string.campos_vacio))
                            .show();
                }

            }
        });
    }

    private void login(){
        progressDialog.show();
        String passC=Cifrar.encriptarHash(pass.getText().toString(),"MD5");
        TelephonyManager telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String idCelular=telephonyManager.getDeviceId();
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("aUsuario",user.getText().toString().trim());
        params.put("aPassword",passC);
        //Falta idCelular

        Log.e(LOG_TAG,"Usuario:"+user.getText().toString().trim()+
                " Password:"+passC);

        client.post(String.valueOf(R.string.ip), params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG,"response:"+ Arrays.toString(responseBody));
                try {
                    JSONObject jsonObject=new JSONObject(Arrays.toString(responseBody));
                    String respuesta=jsonObject.getString("respuesta");
                    if (respuesta.contains("ok")){
                        //Get datos del json
                        int idConductor=jsonObject.getInt("idConductor");
                        int lPasswordTemporal=jsonObject.getInt("lPasswordTemporal");
                        int idUnidad=jsonObject.getInt("idUnidad");
                        int lOcupado=jsonObject.getInt("lOcupado");
                        int idCelular=jsonObject.getInt("idCelular");

                        //Guardar datos de usuario en preferencias
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putInt(getString(R.string.p_id_conductor),idConductor);
                        editor.putInt(getString(R.string.p_id_unidad),idUnidad);
                        editor.putInt(getString(R.string.p_ocupado),lOcupado);
                        editor.putString(getString(R.string.p_nick),user.getText().toString().trim());
                        editor.commit();

                        Intent intent= new Intent(LoginActivity.this, MainActivity.class);
                        if (idCelular<1){
                            intent.addCategory("CambioCelular");//Validar en main
                        }
                        if (lPasswordTemporal<1){
                            intent.addCategory("PasswordTemporal");//Validar en main
                        }
                        progressDialog.dismiss();
                        startActivity(intent);
                        finish();
                    }else {
                        Log.e(LOG_TAG,"response:"+ Arrays.toString(responseBody)+" statusCode:"+statusCode);
                        //Alert dialog con informacion del error
                        progressDialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"response:"+ Arrays.toString(responseBody)+" statusCode:"+statusCode);
                //Alert dialog con informacion del error
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialogCampoVacio!=null && dialogCampoVacio.isShowing()){
            dialogCampoVacio.dismiss();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

}
