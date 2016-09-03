package com.brandonzamudio.softtim.mobilityconductor;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String LOG_TAG="MainActivity";
    GoogleApiClient mGoogleApiClient;
    long MIN_TIME_BW_UPDATES=1000 * 15;
    boolean shouldEnd=false,resumed,isMapReady;
    Location currentLocation;
    isBetterLocation isBetter=new isBetterLocation();
    private GoogleMap mMap;
    LatLng currentLatLng;
    Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.logo);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasCategory("FMS")){
            String message=intent.getStringExtra("whatToDo");
            switch (message){
                case "test":
                    Toast.makeText(MainActivity.this,"Just Testing",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(LOG_TAG,"onNewIntent:"+"whatToDo="+message);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getPeriodicFusedLocation() {
        final Context context = this;

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
                        Log.e("getPeriodicFusedLocatio","SUCCES");
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, finalMLocationRequest, MainActivity.this);

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            Log.e("getPeriodicFusedLocatio","Error in RESOLUTION_REQUIRED");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("¡UPS!")
                                .setMessage("Parece que hay un problema con los requerimientos para utilizar esta aplicacion." +
                                        "Por favor revisa tu dispositivo o tus ajustes antes de continuar.")
                                //Agregar codigo de error
                                .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //Modificar preferencias para que no inicia sesion

                                        shouldEnd = true;

                                        dialog.dismiss();
                                        Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intentm);
                                        Toast.makeText(MainActivity.this, "Hasta pronto", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .show();

                        Log.e("getPeriodicFusedLocatio","Error=SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }

    private void newLocation(){
        Log.e(LOG_TAG,"newLocation");
        if (currentLatLng==null){
            currentLatLng=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }else {
            currentLatLng=null;
            currentLatLng=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
        if (mMap!=null){
            Log.e(LOG_TAG,"newLocation"+"map ready");
            if (currentMarker==null){
                currentMarker=mMap.addMarker(new MarkerOptions().position(currentLatLng));
            }else {
                currentMarker.remove();
                currentMarker=null;
                currentMarker=mMap.addMarker(new MarkerOptions().position(currentLatLng));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,14));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed=true;
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()){
            mGoogleApiClient.connect();
            Log.e(LOG_TAG,"onResume connecting");
        }
        Log.e(LOG_TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed=false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.e(LOG_TAG,"Token: "+FirebaseInstanceId.getInstance().getToken());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(LOG_TAG,"onConnected");
        getPeriodicFusedLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(LOG_TAG,"onLocationChanged");
        //if (isBetter.isBetterLocation(location,currentLocation)){
            currentLocation=location;
            newLocation();
        //}
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("¡UPS!")
                .setMessage("Parece que hay un problema para acceder a tu ubicacion, por favor dejanos intentar de nuevo.")
                //Agregar codigo de error
                .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Modificar o NO modificar preferencias para que VULEVA a inicia sesion

                        shouldEnd = true;

                        dialog.dismiss();
                        Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intentm);
                        finish();
                    }
                })
                .show();

    }
}
