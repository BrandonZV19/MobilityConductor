package com.softtim.mobilityconductor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

//import com.naceware.brandonzamudio.conductores.R;

/**
 * Created by softtim on 15/07/16.
 */
public class BackService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    SharedPreferences preferences;
    int idUnidad, idConductor;
    String LOG_TAG = "BackService";
    int MY_PERMISSIONS_REQUEST_LOCATION = 1, MY_PERMISSIONS_REQUEST_CALL = 9;
    public GoogleApiClient mGoogleApiClient;
    long MIN_TIME_BW_UPDATES = 1000 * 5;
    WakeLocker wakeLocker;
    isBetterLocation isBetter = new isBetterLocation();
    Location currentBestLocation;
    MediaPlayer mp;
    Uri soundCancelado;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //contextt=context;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        soundCancelado = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.cancelado);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        preferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        if (!preferences.contains("idUnidad") || !preferences.contains(getString(R.string.p_id_conductor))) {
            Log.e(LOG_TAG, "not contains");
            notificar(BackService.this, "Hay un problema con tus datos",
                    "Por favor comprueba tu sesión nuevamente. ", 0);
        } else {
            Log.e(LOG_TAG, "contains");

            idUnidad = preferences.getInt(getString(R.string.p_id_unidad), 0);
            idConductor = preferences.getInt((getString(R.string.p_id_conductor)), 0);

            if (idUnidad == 0 || idConductor == 0) {
                Log.e(LOG_TAG, "is 0");
                notificar(BackService.this, "Hay un problema con tus datos",
                        "Por favor comprueba tu sesión nuevamente. ", 0);
            }else {
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                    Log.e(LOG_TAG, "conecting");
                }
            }
        }

        return START_STICKY;
    }

    private void getPeriodicFusedLocation() {

        final Context context = BackService.this;

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        final LocationRequest finalMLocationRequest = mLocationRequest;
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                //final LocationSettingsStates LocationSettingsStates = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.e("getPeriodicFusedLocatio", "SUCCES");

                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            notificar(BackService.this, "Hay un problema con los permisos para Mobility.",
                                    "Por favor habilitalos en la configuracion de tu dispositivo. ", 0);

                        }else {
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, finalMLocationRequest, BackService.this);
                        }

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.e("getPeriodicFusedLocatio", "RESOLUTION");
                        notificar(BackService.this, "Hay un problema con los permisos para Mobility.",
                                "Por favor habilitalos en la configuracion de tu dispositivo. ", 0);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        notificar(BackService.this, "Hay un problema con los permisos para Mobility.",
                                "Por favor habilitalos en la configuracion de tu dispositivo. ", 0);

                        Log.e("getPeriodicFusedLocatio", "Error=SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }


    private void notificar(Context context, String title, String text, int code){
        wakeLocker=new WakeLocker();
        wakeLocker.acquire(context);

        Intent resultIntent = null;
        NotificationCompat.Builder mBuilder = null;

        if (code==0){//Error

            if (mGoogleApiClient!=null){
                if (mGoogleApiClient.isConnected()){
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, BackService.this);
                    mGoogleApiClient.disconnect();
                }
            }

            resultIntent = new Intent(context, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setDefaults(Notification.DEFAULT_VIBRATE)

                            .setContentTitle(title)
                            .setAutoCancel(true)
                            .setContentText(text);


            AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0 /*flags*/);

            if (mp!=null){
                mp.reset();
            }
            mp = new MediaPlayer();
            try {
                mp.setDataSource(this, soundCancelado);
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

            wakeLocker.release();

            startActivity(resultIntent);

            stopSelf();

        }

        if (resultIntent!=null&&mBuilder!=null){
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
            stopSelf();

        }else {
            wakeLocker.release();
            Log.e("ALARM Ntfy","null");
            stopSelf();
        }

    }



    private  void postLocationHere(){
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();

        String url=getString(R.string.mainURL)+"/Servicios/agregar_ubicacion_sp";
        double lat=currentBestLocation.getLatitude();
        double lng=currentBestLocation.getLongitude();
        params.put("dLatitudRegistro",lat);
        params.put("dLongitudRegistro",lng);
        params.put("idUnidad",idUnidad);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG, "backPOST response:"+new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"backPOST statusCode:"+statusCode);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient!=null){
            if (mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, BackService.this);
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onLocationChanged(Location locationn) {
        Log.e("ALARMonLocationChanged", "Provider: " + locationn.getProvider());
        if (isBetter.isBetterLocation(locationn,currentBestLocation)) {
            currentBestLocation = locationn;
            if (idUnidad != 0 && idConductor != 0) {
                //PostHere
                postLocationHere();
            } else {
                Log.e(LOG_TAG, "onLocationChanged: idUnidad 0");
            }

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        notificar(BackService.this, "Hay un problema con los permisos para Mobility.",
                "Por favor habilitalos en la configuracion de tu dispositivo. ", 0);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(LOG_TAG, "onConnected");
        getPeriodicFusedLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        notificar(BackService.this, "Hay un problema con los permisos para Mobility.",
                "Por favor habilitalos en la configuracion de tu dispositivo. ", 0);
    }
}
