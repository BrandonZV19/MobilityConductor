package com.softtim.mobilityconductor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class MapaEmergencia extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    Panico panico;
    int idConductor=0,idUnidad=0;
    double latiM=0,longiM=0,idAlerta=0;
    String placas="",nombre="";
    TextView nombreC, modeloMarcaC, placaC;
    ImageView imagenC, imagenU;
    private Timer myTimer;
    String LOG_TAG = "MapaEmergencia";
    Marker mUnidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        nombreC=(TextView)findViewById(R.id.mapNombreC);
        modeloMarcaC=(TextView)findViewById(R.id.mapMarcaModeloC);
        placaC=(TextView)findViewById(R.id.mapPlacasC);
        imagenC=(ImageView)findViewById(R.id.mapImagenC);
        imagenU=(ImageView)findViewById(R.id.mapImagenU);

        setTitle("Alerta de panico");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        panico = (Panico) getIntent().getSerializableExtra("panic");
        if(panico !=null) {
            String marcaModeloC= panico.getMarca()+"  "+ panico.getModelo();

            idAlerta=panico.getIDalerta();
            nombreC.setText(panico.getNombre());
            modeloMarcaC.setText(marcaModeloC);
            placaC.setText(panico.getPlacas());
            nombre= panico.getNombre();
            latiM= panico.getLatitud();
            longiM= panico.getLongitud();
            placas= panico.getPlacas();
            Picasso.with(MapaEmergencia.this).load("http://softtim.mx/mobilityV2/assets/img/conductores/"+ panico.getImagenC()).into(imagenC);
            Picasso.with(MapaEmergencia.this).load("http://softtim.mx/mobilityV2/assets/img/unidades/"+ panico.getImagenU()).into(imagenU);

            if(mMap!=null){
                LatLng position=new LatLng(latiM,longiM);
                mUnidad=mMap.addMarker(new MarkerOptions().position(position)
                        .title(nombre)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mlocation)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
            }

        }else{
            Log.e(LOG_TAG,"OnCreate" + " Panico null");

        }

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            public void run() {
                TimerMetodo();
            }
        }, 0, 10000);
    }


    private void TimerMetodo() {

        this.runOnUiThread(Timer_Tick);

    }

    private Runnable Timer_Tick = new Runnable() {
        @Override
        public void run() {

            getUbicacionEmergencia();

        }
    };

    public void onPause ()
    {
        super.onPause();
        if (myTimer != null) {
            myTimer.cancel();
        }
    }

    private void getUbicacionEmergencia(){
        Log.e(LOG_TAG, "get before idA " + idAlerta);
        // post: idAlerta retorna dLatitudRegistro dLongitudRegistro

        AsyncHttpClient cliente=new AsyncHttpClient();
        String url=getString(R.string.mainURL)+"/Conductor/ubicacion_alerta";
        RequestParams params=new RequestParams();
        params.put("idAlerta",idAlerta);

        cliente.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.e(LOG_TAG, "get resp " + response);

                try {
                    JSONObject jsonObject =new JSONObject(new String(responseBody));
                    latiM=jsonObject.getDouble("dLatitudRegistro");
                    longiM=jsonObject.getDouble("dLongitudRegistro");

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                LatLng position=new LatLng(latiM,longiM);
                if (mUnidad!=null){
                    //Si ya se encontraba en el mapa
                    //Obtenemos el marker y lo animamos a la nueva locacion

                    LatLng ultima=mUnidad.getPosition();
                    LatLngInterpolator interpolator = new LatLngInterpolator.Spherical();
                    interpolator.interpolate(10,ultima,position);
                    MarkerAnimation.animateMarkerToICS(mUnidad,position,interpolator);
                }else{
                    mUnidad=mMap.addMarker(new MarkerOptions().position(position)
                            .title(nombre)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mlocation)));
                }
                //null pointer ???
                mUnidad.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG, "get statusCode" + statusCode);
                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión " + statusCode + ".\n Por favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(MapaEmergencia.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapaEmergencia.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(MapaEmergencia.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                Snackbar.make(getWindow().getDecorView().getRootView(), "Mobility requiere permisos para acceder a tu ubicación.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
                        .setAction("Configuración", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent i = new Intent();
                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + MapaEmergencia.this.getPackageName()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                MapaEmergencia.this.startActivity(i);
                            }
                        }).show();

                ActivityCompat.requestPermissions(MapaEmergencia.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {

                ActivityCompat.requestPermissions(MapaEmergencia.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            }

        }else {
            mMap.setMyLocationEnabled(true);
        }

        if (latiM!=0 && longiM!=0){
            LatLng position=new LatLng(latiM,longiM);
            if (mUnidad!=null){
                if (mUnidad.isVisible()){
                    mUnidad.remove();
                }
              mUnidad=null;
            }
            mUnidad=mMap.addMarker(new MarkerOptions().position(position)
                    .title(nombre)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mlocation)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,18));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
