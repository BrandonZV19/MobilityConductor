package com.softtim.mobilityconductor;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by softtim on 8/24/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    Realm realm;
    private static final String TAG = "MyFirebaseMsgService";
    SharedPreferences preferences;
    WakeLocker wakeLocker;
    MediaPlayer mp;
    Uri soundNuevoServicio, soundCancelado, soundAlarma1;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getBaseContext().getApplicationContext());

        soundNuevoServicio = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.nuevoserv);
        soundCancelado = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.cancelado);
        soundAlarma1 = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.alarma1);
    }

    /**
     * Called when message is received.
     *
     *      *
     *
     *
     *     Savar cualquier notificacion que no se haya completado su fucnion correctamente para
     *     recuperarla en cualquier estado de la aplicacion    *********************************
     *
     *
     *      *
     *
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        preferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

                // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Borrar el servicio de la BD y actualizar la interfaz de serivicios disponibles para que desaparezca
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());


            //Push para invalidar sesion


            if (remoteMessage.getData().get("message").contains("servicio_descartado")){

                RealmConfiguration config = new RealmConfiguration
                        .Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build();

                Realm.setDefaultConfiguration(config);

                realm=Realm.getDefaultInstance();
                //realm.setAutoRefresh(true);

                final int idd=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_id)));

                final RealmResults<Servicio> results = realm.where(Servicio.class).equalTo("id",idd).findAll();

                // All changes to data must happen in a transaction
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        results.deleteAllFromRealm();
                    }
                });
                realm.close();

            }

            if (remoteMessage.getData().get("message").contains("alerta_panico")){

                /*

                idPanicoServicio
                dLatitudAlerta
                dLongitudAlerta
                fFechaHoraAltaAlerta
                aNombreConductor
                aApellidoPatConductor
                aApellidoMatConductor
                aMarca
                aModelo
                aPlacas
                aFotografiaUnidad
                aFotografia

                */



                final int id=Integer.parseInt(remoteMessage.getData().get("idPanicoServicio"));
                final double lat=Float.parseFloat(remoteMessage.getData().get("dLatitudAlerta"));
                final double lng=Float.parseFloat(remoteMessage.getData().get("dLongitudAlerta"));
                final String fecha=remoteMessage.getData().get("fFechaHoraAltaAlerta");

                final String nombre=remoteMessage.getData().get("aNombreConductor");
                final String nombre2=remoteMessage.getData().get("aApellidoPatConductor");
                final String nombre3=remoteMessage.getData().get("aApellidoMatConductor");
                final String marca=remoteMessage.getData().get("aMarca");
                final String modelo=remoteMessage.getData().get("aModelo");
                final String placas=remoteMessage.getData().get("aPlacas");
                final String fotoCon=remoteMessage.getData().get("aFotografia");
                final String fotoUn=remoteMessage.getData().get("aFotografiaUnidad");

                final Panico panic=new Panico();
                panic.setIDalerta(id);
                panic.setImagenC(fotoCon);
                panic.setImagenU(fotoUn);
                panic.setLongitud(lng);
                panic.setLatitud(lat);
                panic.setFecha(fecha);
                panic.setNombre(nombre+" "+nombre2+" "+nombre3);
                panic.setMarca(marca);
                panic.setModelo(modelo);
                panic.setPlacas(placas);

                RealmConfiguration config = new RealmConfiguration
                        .Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build();

                Realm.setDefaultConfiguration(config);

                realm=Realm.getDefaultInstance();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(panic);
                    }
                });

                realm.close();



                whatToDo("alerta_panico");
                sendNotification("alerta_panico");
            }

            if (remoteMessage.getData().get("message").contains("servicio_cancelado")){

                RealmConfiguration config = new RealmConfiguration
                        .Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build();

                Realm.setDefaultConfiguration(config);

                realm=Realm.getDefaultInstance();
                //realm.setAutoRefresh(true);

                final int idd=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_id)));
                final int stat=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_status)));

                final RealmResults<Servicio> results = realm.where(Servicio.class).equalTo("id",idd).findAll();

                // All changes to data must happen in a transaction
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if (stat>1){
                            //Se guarda como cancelado
                            Servicio result = realm.where(Servicio.class).equalTo("id",idd).findFirst();
                            result.setCancelado(1);// Error
                            //Se manda con status 2 a todos. pasar a cancelado
                            realm.insertOrUpdate(result);
                        }
                        if (stat==1){
                            results.deleteAllFromRealm();
                        }

                    }
                });
                realm.close();
                if (stat>1){
                    whatToDo("servicio_cancelado");
                    sendNotification("servicio_cancelado");
                }else {
                    whatToDo("servicio_descartado");
                }

            }

            if (remoteMessage.getData().get("message").contains("invalidar_sesion")){
                //El id del cel no corresponde
                //Supuestamente nunca pasara, no se deja iniciar sesion en otro cel.
                //Cuando solicite el permiso para hacerlo es cuando se recibe esta push
            }

            if (remoteMessage.getData().get("message").contains("test")){
                whatToDo("test");
                sendNotification("test");
            }

            if (remoteMessage.getData().get("message").contains("request_paypal")){
                whatToDo("request_paypal");
                sendNotification("request_paypal");
            }

            if (remoteMessage.getData().get("message").contains("paypal_acreditado")){
                whatToDo("paypal_acreditado");
                sendNotification("paypal_acreditado");
            }


            if (remoteMessage.getData().get("message").contains("solicitud_servicio")){

                //Optimizar y actualizar datos del servicio, incluir colonia
                //mandar colonia desde que se crea el servicio por solic del usuario

                final int id=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_id)));
                //final int cancelado=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_cancelado)));
                //final int cancelado_v=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_cancelado_visto)));
                //final int confirmado=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_confirmado)));
                final String email=remoteMessage.getData().get(getString(R.string.p_s_email));
                final String fecha=remoteMessage.getData().get(getString(R.string.p_s_fecha));

                final double lat_d=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lat_destino)));
                final double lng_d=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lng_destino)));
                final double lat_o=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lat_origen)));
                final double lng_o=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lng_origen)));

                final String nombre=remoteMessage.getData().get(getString(R.string.p_s_nombre));
                final String referencia=remoteMessage.getData().get(getString(R.string.p_s_referencia));
                final int status=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_status)));
                final String tel=remoteMessage.getData().get(getString(R.string.p_s_telefono));
                final String dir_d=remoteMessage.getData().get(getString(R.string.p_s_dir_destino));
                final String col_d=remoteMessage.getData().get(getString(R.string.p_s_col_destino));
                final String dir_o=remoteMessage.getData().get(getString(R.string.p_s_dir_origen));
                final String col_o=remoteMessage.getData().get(getString(R.string.p_s_col_origen));


                final Servicio servicio = new Servicio();
                servicio.setId(id);

                servicio.setLat_d(lat_d);
                servicio.setLng_d(lng_d);
                servicio.setLat_o(lat_o);
                servicio.setLng_o(lng_o);

                servicio.setEmail(email);
                servicio.setFecha(fecha);
                servicio.setDir_dest(dir_d);
                servicio.setColonia_dest(col_d);
                servicio.setDir_orig(dir_o);
                servicio.setColonia_orig(col_o);

                servicio.setNombre(nombre);
                servicio.setReferencia(referencia);
                servicio.setStatus(status);
                servicio.setTel(tel);

                RealmConfiguration config = new RealmConfiguration
                        .Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build();

                Realm.setDefaultConfiguration(config);

                realm=Realm.getDefaultInstance();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // This will create a new object in Realm or throw an exception if the
                        // object already exists (same primary key)
                        // realm.copyToRealm(obj);

                        // This will update an existing object with the same primary key
                        // or create a new one if the primary key already exists
                        realm.copyToRealmOrUpdate(servicio);
                    }
                });

                //No agreguar current service sino hasta aceptar

                realm.close();
                whatToDo("solicitud_servicio");
                sendNotification("solicitud_servicio");

            }

        }

        // Check if message contains a notification payload.
        //Aqui estoy recibiendo de consola
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getBody().contains("agregar")){

            }

            if (remoteMessage.getNotification().getBody().contains("eliminar")){


            }

            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received
     *
     *                    {lConfirmadoConductor=1,
     *                    lCancelado=0,
     *                    aApellidoPatUsuario=Victoria,
     *                    aEmail=brandon.azv@gmail.com,
     *                    aApellidoMatUsuario=Zirangua,
     *                    idStatus=5,
     *                    dLongitudOrigen=-101.165425,
     *                    idUnidad=2,
     *                    dLatitudDestino=19.704325,
     *                    aNombreUsuario=Sandra,
     *                    aTelefono=1234567890,
     *                    lDescartado=0,
     *                    lCanceladoVisto=0,
     *                    dLongitudDestino=-101.18273099999999,
     *                    aReferencia=Lunes 3 5,
     *                    message=solicitud_servicio,
     *                    fFechaHoraSolicitud=2016-07-04 11:41:53,
     *                    idServicio=500,
     *                    dLatitudOrigen=19.685273}
     */




    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addCategory("FMS");

        if (messageBody.contains("solicitud_servicio")){
            intent.putExtra("whatToDo",messageBody);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            //Uri sound = Uri.parse("android.resource://com.brandonzamudio.softtim.mobilityconductor/" + R.raw.taximine);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logosimpleblack)
                    .setContentTitle("¡Nueva solicitud de servicio!")
                    .setAutoCancel(true)
                    //.setSound(sound)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
        }

        if (messageBody.contains("test")){
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logo512c)
                    .setContentTitle("Notificación de testeo")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
        }

        if (messageBody.contains("request_paypal")){
            intent.putExtra("whatToDo",messageBody);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logosimpleblack)
                    .setContentTitle("Tu cliente ha solicitado un pago electrónico")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
        }

        if (messageBody.contains("alerta_panico")){
            intent.putExtra("whatToDo",messageBody);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logosimpleblack)
                    .setContentTitle("Alguien ha emitido una alerta de panico")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
        }

        if (messageBody.contains("paypal_acreditado")){
                intent.putExtra("whatToDo",messageBody);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logosimpleblack)
                        .setContentTitle("Se ha acreditado el pago de tu servicio")
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
            }

        if (messageBody.contains("servicio_cancelado")){
            intent.putExtra("whatToDo",messageBody);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logosimpleblack)
                    .setContentTitle("Tu servicio ha sido cancelado")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
        }


    }

    private void whatToDo(String messageBody){
        wakeLocker=new WakeLocker();
        wakeLocker.acquire(getApplicationContext());
        String classRunning;

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        Log.e("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        classRunning=componentInfo.getClassName();

        if (messageBody.contains("test")){
            if (classRunning.contains("MainActivity")){
                Intent intent = new Intent(getBaseContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory("FMS");
                intent.putExtra("whatToDo",messageBody);
                getApplication().startActivity(intent);
            }

        }

        if (messageBody.contains("solicitud_servicio")){

            AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0 /*flags*/);

            if (mp!=null){
                mp.reset();
            }
            mp = new MediaPlayer();
            try {
                mp.setDataSource(this, soundNuevoServicio );
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);

            mp.prepareAsync();

            mp.setVolume(1.0f, 1.0f);
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
            mp.setAudioSessionId(1); //manually assign an ID here

            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

                Intent intent = new Intent(getBaseContext(), ServiciosDisponibles.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory("FMS");
                intent.putExtra("whatToDo",messageBody);
                getApplication().startActivity(intent);

        }

        if (messageBody.contains("servicio_descartado")){
            //falta : confirmar el visto cancelado en el metodo que comprueba el status actual del panico en el MAIN
            //Segun el estatus, 2 notifica, 1 solo elimina
            if (classRunning.contains("MainActivity")){
                Intent intent = new Intent(getBaseContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory("FMS");
                intent.putExtra("whatToDo",messageBody);
                getApplication().startActivity(intent);
            }else if (classRunning.contains("ServiciosDisponibles")){
                Intent intent = new Intent(getBaseContext(), ServiciosDisponibles.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory("FMS");
                intent.putExtra("whatToDo",messageBody);
                getApplication().startActivity(intent);
            }
        }

        if (messageBody.contains("alerta_panico")){
            Intent intent = new Intent(getBaseContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("FMS");
            intent.putExtra("whatToDo",messageBody);
            getApplication().startActivity(intent);
        }

        if (messageBody.contains("request_paypal")){
            //falta : confirmar el visto cancelado en el metodo que comprueba el status actual del panico en el MAIN
            //Segun el estatus, 2 notifica, 1 solo elimina
            Intent intent = new Intent(getBaseContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("FMS");
            intent.putExtra("whatToDo",messageBody);
            getApplication().startActivity(intent);
        }

        if (messageBody.contains("paypal_acreditado")){
            //falta : confirmar el visto cancelado en el metodo que comprueba el status actual del panico en el MAIN
            //Segun el estatus, 2 notifica, 1 solo elimina
            Intent intent = new Intent(getBaseContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("FMS");
            intent.putExtra("whatToDo",messageBody);
            getApplication().startActivity(intent);
        }

        if (messageBody.contains("servicio_cancelado")){
            //falta : confirmar el visto cancelado en el metodo que comprueba el status actual del panico en el MAIN
            //Segun el estatus, 2 notifica, 1 solo elimina

            AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0 /*flags*/);

            if (mp!=null){
                mp.reset();
            }
            mp = new MediaPlayer();
            try {
                mp.setDataSource(this, soundCancelado );
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);

            mp.prepareAsync();

            mp.setVolume(1.0f, 1.0f);
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
            mp.setAudioSessionId(1); //manually assign an ID here

            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

                Intent intent = new Intent(getBaseContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory("FMS");
                intent.putExtra("whatToDo",messageBody);
                getApplication().startActivity(intent);

        }

        if (messageBody.contains("alerta_panico")){
            //falta : confirmar el visto cancelado en el metodo que comprueba el status actual del panico en el MAIN
            //Segun el estatus, 2 notifica, 1 solo elimina

            AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0 /*flags*/);

            if (mp!=null){
                mp.reset();
            }
            mp = new MediaPlayer();
            try {
                mp.setDataSource(this, soundAlarma1 );
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);

            mp.prepareAsync();

            mp.setVolume(1.0f, 1.0f);
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
            mp.setAudioSessionId(1); //manually assign an ID here

            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            Intent intent = new Intent(getBaseContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("FMS");
            intent.putExtra("whatToDo",messageBody);
            getApplication().startActivity(intent);

        }

        wakeLocker.release();

    }

}
