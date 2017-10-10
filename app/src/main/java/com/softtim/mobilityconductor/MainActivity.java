package com.softtim.mobilityconductor;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.Log;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String LOG_TAG = "UMainActivity";
    String timeAprox;
    HashMap<Integer,Marker> markerHashMap;
    View vLugar;
    TextView vlTitulo,vlDescripcion,vlHorario,vlDireccion;
    ImageView vlImagen;
    AlertDialog dialogLugar;
    int MY_PERMISSIONS_REQUEST_LOCATION=1, MY_PERMISSIONS_REQUEST_CALL = 9;
    GoogleApiClient mGoogleApiClient;
    long MIN_TIME_BW_UPDATES = 1000 * 5;
    isBetterLocation isBetter = new isBetterLocation();
    boolean shouldEnd = false, resumed, currentValid=false, isFirstTime=true, delay=false,
            wentSettings=false;
    Location currentLocation, currentBestLocation;
    private GoogleMap mMap;
    LatLng currentLatLng, lasLatLng, orig,dest;
    Marker mOrigen,mDestino,mUsuario;
    Circle mCircle;
    SharedPreferences preferences;
    int idUnidad, idConductor, currentService,currentStatus,currentConfirmado,currentCancelado,
            currentCanceladoV, currentOcupado, currentCostoC,currentCostoU,currentCostoS,currentLlegoOrigen,
            currentSolicitadoPP,currentCostoPP,currentAcreditadoPP,numAlertas,count2=0, lOcupado;
    float currentCalificado;
    AlertDialog dialogiIniciaSesion,calificarEfectivo,calificarElectronico,ingresaCostoPP,dialogServicio
            ,optionsNavi;
    Realm realm;
    TextView username, tipoUnidad, sideIdUnidad, estado, naviTW,naviTG;
    ImageView naviW,naviG;
    Button bStatus, bCancelar;
    ImageButton llego,panic,navig;
    ProgressDialog progressDialog;
    Servicio currentRealmService;
    Polyline polyline;
    LayoutInflater inflater;
    View promptCEfe,promptCEle,promptNavi;
    EditText comentariosE, costoE,comentariosEle, costoEle,costoIPP,tvDenue;
    RatingBar ratingE,ratingEle;
    NavigationView navigationView;
    Menu navigationMenu;
    Intent intentService,serviceBlueIntent;
    View promptDSS;
    TextView dsidserv, dshorasol, dsnameuser, dstel, dsmail, dsorig, dsdest, dsrefer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(getApplicationContext());

        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.simplebar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        intentService = new Intent(MainActivity.this, BackService.class);
        serviceBlueIntent = new Intent(MainActivity.this, BlueService.class);

        preferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        username = (TextView) headerView.findViewById(R.id.sideUsername);
        tipoUnidad=(TextView)headerView.findViewById(R.id.sideTipoUnidad);
        sideIdUnidad=(TextView)headerView.findViewById(R.id.sideIdUnidad);

        estado=(TextView)findViewById(R.id.tvEstado);
        estado.setVisibility(View.GONE);

        if (!preferences.contains("idUnidad") || !preferences.contains(getString(R.string.p_id_conductor))) {
            Log.e(LOG_TAG, "not contains");

            //del posible pref
            if (mGoogleApiClient!=null){
                if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                }
            }


            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(getString(R.string.p_id_unidad));
            editor.remove(getString(R.string.p_id_conductor));
            editor.remove(getString(R.string.p_username));
            editor.remove(getString(R.string.p_pass));
            editor.remove(getString(R.string.p_nick));
            editor.remove("active");
            editor.commit();

            shouldEnd = true;

            isFirstTime=false; //para evitar que entre en Onresume y se quede el dialogo

            if (preferences.contains("active")) {
                Log.e(LOG_TAG, "contains active");
                dialogiIniciaSesion = null;
                dialogiIniciaSesion = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("¡UPS!")
                        .setMessage("Por favor inicia sesión") //pasar a strings
                        .setCancelable(false)
                        //Agregar codigo de error
                        .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shouldEnd = true;

                                dialog.dismiss();
                                Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intentm);
                                finish();
                            }
                        })
                        .show();
            } else {
                Log.e(LOG_TAG, "not active");
                Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intentm);
                finish();
            }

        } else {

            Log.e(LOG_TAG, "contains");

            idUnidad = preferences.getInt(getString(R.string.p_id_unidad), 0);
            idConductor = preferences.getInt((getString(R.string.p_id_conductor)),0);

            Log.e(LOG_TAG, "l1 idc:"+idConductor +" idu:"+idUnidad);
            if (idUnidad==0 || idConductor==0) {
                Log.e(LOG_TAG, "l2 idc:"+idConductor +" idu:"+idUnidad);
                if (preferences.contains("active")) {
                    dialogiIniciaSesion = null;
                    dialogiIniciaSesion = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("¡UPS!")
                            .setMessage("Por favor inicia sesión") //pasar a strings
                            .setCancelable(false)
                            //Agregar codigo de error
                            .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    shouldEnd = true;

                                    dialog.dismiss();
                                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intentm);
                                    finish();
                                }
                            })
                            .show();
                } else {
                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intentm);
                    finish();
                }
            }

            lOcupado=preferences.getInt(getString(R.string.current_ocupado), 0);

            if (lOcupado==0){
                //Desocupado

            }else {
                //Ocupado
                bStatus.setText("OCUPADO");
                bStatus.setTextColor(Color.BLACK);
                bStatus.setBackgroundColor(Color.parseColor("#ffc107"));
                bStatus.setVisibility(View.VISIBLE);

                estado.setEnabled(false);
                estado.setVisibility(View.GONE);

                SharedPreferences.Editor editor=preferences.edit();
                editor.putInt(getString(R.string.current_ocupado),1);
                editor.commit();
            }

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }

            String un="User";
            if (preferences.contains(getString(R.string.p_username))){
                un=preferences.getString(getString(R.string.p_username),"User");
                username.setText(un);
            }else if (preferences.contains(getString(R.string.p_nick))){
                un=preferences.getString(getString(R.string.p_nick),"User");
                username.setText(un);

            }

            String ti="User";
            if (preferences.contains(getString(R.string.p_tipo_unidad))){
                ti=preferences.getString(getString(R.string.p_tipo_unidad),"Unidad");
                tipoUnidad.setText(ti);
            }

            sideIdUnidad.setText("Unidad "+idUnidad);

            inflater = MainActivity.this.getLayoutInflater();

            //Creacion del dialog requestPP
            //FALTA guardar esta vista para que el conductor pueda ingresarla en otro momento
            //ingresar el costo para que el cliente pague por paypal
            View costoPP = inflater.inflate(R.layout.ingresar_costo_pp, null);
            costoIPP=(EditText) costoPP.findViewById(R.id.eCostoPP);
            costoIPP.requestFocus();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(Html.fromHtml("<font color='#000000'>Ingresa el monto a cobrar por el servicio</font>"))
                    .setMessage("Tu cliente ha solicitado un pago electronico, necesitamos que indiques la cantidad a cobrar.")
                    .setCancelable(false)
                    .setView(costoPP)
                    .setPositiveButton("Aceptar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, int id) {

                                    if (progressDialog!=null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                        Log.e("dismiss in","reuqestpayment before");
                                    }
                                    progressDialog=null;
                                    if(MainActivity.this.getParent()!=null){
                                        progressDialog=new ProgressDialog(MainActivity.this.getParent());
                                    }else{
                                        progressDialog=new ProgressDialog(MainActivity.this);
                                    }
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setCancelable(false);
                                    progressDialog.setTitle("Loading...");
                                    if(!MainActivity.this.isFinishing()) {
                                        progressDialog.show();
                                        Log.e("show in","");
                                    }

                                    if (costoIPP.getText().toString().trim().length()>1){
                                        //webservice post costopp
                                        try {
                                        AsyncHttpClient cliente = new AsyncHttpClient();
                                        RequestParams parametros = new RequestParams();
                                        String url = getString(R.string.mainURL)+"/Servicios/conductor_costo_paypal";
                                        parametros.put("idServicio", currentService);
                                        parametros.put("dCostoPaypal", costoIPP.getText().toString().trim());

                                        Log.e(LOG_TAG," montoPP prePost idServicio "+currentService +" costoPP "+costoIPP.getText().toString().trim());

                                        cliente.post(url, parametros, new AsyncHttpResponseHandler() {

                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                String resp=new String(responseBody);
                                                Log.e(LOG_TAG,"costoPP success"+ resp);


                                                    currentCostoPP = Integer.parseInt(costoIPP.getText().toString().trim());
                                                    SharedPreferences.Editor editor=preferences.edit();
                                                    editor.putInt(getString(R.string.current_acreditado_pp),0);
                                                    editor.putInt(getString(R.string.current_costo_pp),Integer.parseInt(costoIPP.getText().toString().trim()));
                                                    editor.putInt(getString(R.string.current_solicitado_pp),1);
                                                    editor.commit();

                                                    dialog.dismiss();
                                                    ingresaCostoPP.dismiss();
                                                    ingresaCostoPP.cancel();
                                                    dialog.cancel();
                                                    progressDialog.dismiss();
                                                Log.e("dismiss in", "costoPP after");

                                                    Log.e(LOG_TAG, "From 6");
                                                    getCurrentStatus();

                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                progressDialog.dismiss();
                                                Log.e("dismiss in", "costoPP fail");
                                                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        });
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            Log.e(LOG_TAG,"costoPP catch "+e.toString());
                                            Snackbar.make(getWindow().getDecorView().getRootView(), "Por favor ingresa la cantidad correcta", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }


                                    }else{
                                        Snackbar.make(getWindow().getDecorView().getRootView(), "Por favor ingresa la cantidad correcta", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                }
                            });
            ingresaCostoPP=builder.create();

            //creacion del dialog calificarEfectivo
            //Cobrar en efectivo
            promptCEfe = inflater.inflate(R.layout.calificacion_efectivo, null);
            ratingE=(RatingBar) promptCEfe.findViewById(R.id.ratingE);
            costoE=(EditText) promptCEfe.findViewById(R.id.cantidadE);
            costoE.requestFocus();
            comentariosE=(EditText) promptCEfe.findViewById(R.id.comentariosE);

            AlertDialog.Builder builderCEfe = new AlertDialog.Builder(MainActivity.this)
                    .setView(promptCEfe)
                    .setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            if (progressDialog!=null && progressDialog.isShowing()) {             progressDialog.dismiss();             Log.e("dismiss in","c stat before");         }          progressDialog=null;          if(MainActivity.this.getParent()!=null){             progressDialog=new ProgressDialog(MainActivity.this.getParent());         }else{             progressDialog=new ProgressDialog(MainActivity.this);         }          progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);         progressDialog.setCancelable(false);         progressDialog.setTitle("Loading...");          if(!MainActivity.this.isFinishing()) {             progressDialog.show();             Log.e("show in"," c stat before");         }

                            try{

                            double calif=ratingE.getNumStars();
                            String comens=comentariosE.getText().toString();
                            double cost=0;
                            if (costoE.getText().toString().trim().length()>1){
                                cost=Double.parseDouble(costoE.getText().toString());
                            }


                            if (isSomethingNull(calif,comens,cost) || ratingE.getNumStars()==0 || cost<10){
                                //Faltan datos
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(Html.fromHtml("<font color='#000000'>Por favor completa la calificacion y el costo correctamente</font>"))
                                        .setCancelable(false)
                                        .setNeutralButton("Aceptar",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        progressDialog.dismiss();
                                                        Log.e("dismiss in", "calif wrong");
                                                    }
                                                }).show();
                            }else {

                                AsyncHttpClient cliente = new AsyncHttpClient();
                                RequestParams parametros = new RequestParams();
                                String url = getString(R.string.mainURL)+"/Servicios/calificacion_conductor";
                                parametros.put("idServicio", currentService);
                                parametros.put("dCalificacionUsuario", calif);//Calificacion asiganada para el usuario por el usuario
                                parametros.put("aComentarioConductor", comens);
                                parametros.put("dCostoConductor", cost);

                                Log.e(LOG_TAG," calificarServcicio idServicio "+currentService +" costo "+cost +" calif "+calif);

                                final double finalCost = cost;
                                final double finalCalif = calif;
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        currentRealmService.setCalif_serv(finalCalif);
                                        currentRealmService.setCostoCond(finalCost);
                                    }
                                });

                                cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        String response=new String(responseBody);

                                        Log.e(LOG_TAG," calificarServcicio Reponse: "+response);
                                        progressDialog.dismiss();
                                        Log.e("dismiss in", "calif after");
                                        calificarEfectivo.dismiss();
                                        dialogInterface.dismiss();
                                        statusLibre();
                                        finish();
                                        startActivity(getIntent());
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Log.e("calificarServicio","Failed "+statusCode);
                                        progressDialog.dismiss();
                                        Log.e("dismiss in", "calif fail");
                                        Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                });

                            }

                            }catch (Exception e){
                                e.printStackTrace();
                                Log.e(LOG_TAG,"costoPP catch "+e.toString());
                                progressDialog.dismiss();
                                Log.e("dismiss in", "calif exc");
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Por favor ingresa los valores correctamente", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }

                        }
                    });
            if (promptCEfe.getParent()==null){
                calificarEfectivo=builderCEfe.create();
            }else{
                promptCEfe= null; //set it to null
                // now initialized yourView and its component again
                promptCEfe = inflater.inflate(R.layout.calificacion_efectivo, null);
                ratingE=(RatingBar) promptCEfe.findViewById(R.id.ratingE);
                costoE=(EditText) promptCEfe.findViewById(R.id.cantidadE);
                costoE.requestFocus();
                comentariosE=(EditText) promptCEfe.findViewById(R.id.comentariosE);
                calificarEfectivo=builderCEfe.create();
            }

            //Creacion del dialog calificarElectronico
            promptCEle = inflater.inflate(R.layout.calificacion_pagado, null);
            ratingEle=(RatingBar) promptCEle.findViewById(R.id.ratingP);
            comentariosEle=(EditText) promptCEle.findViewById(R.id.comentariosP);

            AlertDialog.Builder builderCEle = new AlertDialog.Builder(MainActivity.this)
                    .setView(promptCEle)
                    .setCancelable(false)
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            if (progressDialog!=null && progressDialog.isShowing()) {             progressDialog.dismiss();             Log.e("dismiss in","c stat before");         }          progressDialog=null;          if(MainActivity.this.getParent()!=null){             progressDialog=new ProgressDialog(MainActivity.this.getParent());         }else{             progressDialog=new ProgressDialog(MainActivity.this);         }          progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);         progressDialog.setCancelable(false);         progressDialog.setTitle("Loading...");          if(!MainActivity.this.isFinishing()) {             progressDialog.show();             Log.e("show in","");         }

                            try{

                            double calif=ratingEle.getNumStars();
                            String comens=comentariosEle.getText().toString();

                            if (isSomethingNull(calif,comens) || ratingEle.getNumStars()==0 ){
                                //Faltan datos
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(Html.fromHtml("<font color='#000000'>Por favor completa la calificacion y el costo correctamente</font>"))
                                        .setCancelable(false)
                                        .setNeutralButton("Aceptar",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                        progressDialog.dismiss();
                                                        Log.e("dismiss in", "calif wrong 2");
                                                    }
                                                }).show();
                            }else {

                                AsyncHttpClient cliente = new AsyncHttpClient();
                                RequestParams parametros = new RequestParams();
                                String url = getString(R.string.mainURL)+"/Servicios/calificacion_conductor";
                                parametros.put("idServicio", currentService);
                                parametros.put("dCalificacionUsuario", calif);//Calificacion asiganada para el usuario por el conductor
                                parametros.put("aComentarioConductor", comens);

                                Log.e(LOG_TAG," calificarServcicio idServicio "+currentService +"calif "+calif);

                                final double finalCalif=calif;
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        currentRealmService.setCalif_serv(finalCalif);
                                    }
                                });


                                cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        String response=new String(responseBody);

                                        Log.e(LOG_TAG," calificarServcicio Reponse: "+response);
                                        progressDialog.dismiss();
                                        Log.e("dismiss in", "calif after 2");
                                        calificarElectronico.dismiss();
                                        dialogInterface.dismiss();
                                        statusLibre();
                                        finish();
                                        startActivity(getIntent());
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Log.e("calificarServicio","Failed "+statusCode);
                                        progressDialog.dismiss();
                                        Log.e("dismiss in", "calif fail 2");
                                        Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                });
                            }

                            }catch (Exception e){
                                e.printStackTrace();
                                Log.e(LOG_TAG,"costoPP catch "+e.toString());
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Por favor ingresa los valores correctamente", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }

                        }
                    });
            if (promptCEle.getParent()==null){
                calificarElectronico=builderCEle.create();
            }else{
                promptCEle= null; //set it to null
                // now initialized yourView and its component again
                promptCEle = inflater.inflate(R.layout.calificacion_pagado, null);
                ratingEle=(RatingBar) promptCEle.findViewById(R.id.ratingP);
                comentariosEle=(EditText) promptCEle.findViewById(R.id.comentariosP);
                calificarElectronico=builderCEle.create();
            }

            //Dialog opciones de navegacion externas
            promptNavi = inflater.inflate(R.layout.options_navigation, null);
            naviW=(ImageView)promptNavi.findViewById(R.id.gnIW);
            naviG=(ImageView)promptNavi.findViewById(R.id.gnIGM);
            naviTW=(TextView)promptNavi.findViewById(R.id.gnTW);
            naviTG=(TextView)promptNavi.findViewById(R.id.gnTGM);

            AlertDialog.Builder builderNavi = new AlertDialog.Builder(MainActivity.this)
                    .setView(promptNavi);

            if (promptNavi.getParent()==null){
                optionsNavi=builderNavi.create();
            }else{
                promptNavi= null; //set it to null
                // now initialized yourView and its component again
                promptNavi = inflater.inflate(R.layout.options_navigation, null);
                naviW=(ImageView)promptNavi.findViewById(R.id.gnIW);
                naviG=(ImageView)promptNavi.findViewById(R.id.gnIGM);
                naviTW=(TextView)promptNavi.findViewById(R.id.gnTW);
                naviTG=(TextView)promptNavi.findViewById(R.id.gnTGM);

                optionsNavi=builderNavi.create();
            }

            Log.e(LOG_TAG, "From 7");
            getCurrentStatus();
            startService(serviceBlueIntent);
        }

        bStatus=(Button)findViewById(R.id.bStatus);
        bStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bStatusAction();
            }
        });

        bCancelar=(Button)findViewById(R.id.bCancelar);
        bCancelar.setEnabled(false);
        bCancelar.setVisibility(View.GONE);
        bCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Webservice cancelar servicio
                requestCancelarServ();
            }
        });

        llego=(ImageButton)findViewById(R.id.bLlego);
        llego.setVisibility(View.GONE);
        llego.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestChangeStatus(2);
            }
        });

        navig=(ImageButton)findViewById(R.id.goNavigation);
        navig.setVisibility(View.GONE);
        navig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsNavi.show();
            }
        });


        panic=(ImageButton)findViewById(R.id.bPanic);
        panic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO falta
                //Webservice para informar sobre el conductor, el webservice manda la alerta a todas las demas unidades

                panicRequest();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        markerHashMap=new HashMap<>();

        tvDenue=(EditText)findViewById(R.id.tvDenue);
        tvDenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEvent();
            }
        });
        tvDenue.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode()) { // match ENTER key
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    searchEvent();

                    return true; // indicate that we handled event, won't propagate it
                }
                return false;
            }
        });

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    public static Boolean isSomethingNull(Object... objects) {
        for (Object o: objects) {
            if (o == null) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private void searchEvent() {
        //mMap.clear();

        String type;
        if (tvDenue.getText()!=null && tvDenue.getText().length()>0){
            type=(tvDenue.getText().toString().trim().toLowerCase());
        }else {
            type=" ";
        }
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //dialog s

        if (type.trim().length()>0){

            getDenue(currentLocation,type.toLowerCase().trim());

        }

        realm.close();
    }

    private void getDenue(Location location, String var){
        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            Log.e("dismiss in", "denue before");
        }

        progressDialog=null;

        if(MainActivity.this.getParent()!=null){
            progressDialog=new ProgressDialog(MainActivity.this.getParent());
        }else{
            progressDialog=new ProgressDialog(MainActivity.this);
        }

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Cargando...");

        if(!MainActivity.this.isFinishing())
        {
            progressDialog.show();
        }

        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();

        double lat=location.getLatitude();
        double lng=location.getLongitude();
        String url="http://www3.inegi.org.mx/sistemas/api/denue/v1/consulta/buscar/"+var+"/"+lat+","+lng+"/1000/017fe03f-f989-4b99-81d7-489610567a52";
        client.get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG, "Denue response:"+new String(responseBody));

                try{
                    JSONArray arrayJSONLugares= new JSONArray(new String(responseBody));

                    for (int x=0; x<arrayJSONLugares.length(); x++){
                        //TODO:
                        //Llenar la base de datos de lugares con cada json y sus datos
                        // vistas y opciones para los mismos
                        double latitud,longitud;
                        int id;
                        String nombre,descripcion,direccion,categoria
                                ,sitio,correo,telefono,d1,d2,d3,d4,d5;

                        id=     arrayJSONLugares.getJSONObject(x).getInt("Id");
                        nombre=     arrayJSONLugares.getJSONObject(x).getString("Nombre");
                        descripcion=arrayJSONLugares.getJSONObject(x).getString("Razon_social");
                        //imagen=     arrayJSONLugares.getJSONObject(x).getString("aImagenLugar");

                        d1=  arrayJSONLugares.getJSONObject(x).getString("Tipo_vialidad");
                        d2=  arrayJSONLugares.getJSONObject(x).getString("Calle");
                        d3=  arrayJSONLugares.getJSONObject(x).getString("Num_Exterior");
                        d4=  arrayJSONLugares.getJSONObject(x).getString("Colonia");
                        d5=  arrayJSONLugares.getJSONObject(x).getString("CP");

                        direccion=d1+" "+d2+" "+d3+" "+d4+" "+d5;

                        categoria=  arrayJSONLugares.getJSONObject(x).getString("Clase_actividad");

                        if (arrayJSONLugares.getJSONObject(x).isNull("Sitio_internet")){
                            sitio="No disponible";
                        }else {
                            sitio=  arrayJSONLugares.getJSONObject(x).getString("Sitio_internet");
                        }

                        if (arrayJSONLugares.getJSONObject(x).isNull("Correo_e")){
                            correo="No disponible";
                        }else {
                            correo=  arrayJSONLugares.getJSONObject(x).getString("Correo_e");
                        }

                        if (arrayJSONLugares.getJSONObject(x).isNull("Telefono")){
                            telefono="No disponible";
                        }else {
                            telefono=  arrayJSONLugares.getJSONObject(x).getString("Telefono");
                        }
                        latitud=  arrayJSONLugares.getJSONObject(x).getDouble("Latitud");
                        longitud=  arrayJSONLugares.getJSONObject(x).getDouble("Longitud");

                        if (sitio==null || !sitio.contains(".")){
                            sitio="No disponible";
                        }

                        if (correo==null || !correo.contains("@")){
                            correo="No disponible";
                        }

                        if (telefono==null || telefono.trim().length()<7){
                            telefono="No disponible";
                        }

                        final Lugar lugar=new Lugar();
                        lugar.setId(id);
                        lugar.setNombre(nombre);
                        lugar.setDescripcion(descripcion);
                        lugar.setDireccion(direccion);
                        lugar.setCategoria(categoria);
                        lugar.setLatitud(latitud);
                        lugar.setLongitud(longitud);
                        lugar.setSitio(sitio);
                        lugar.setCorreo(correo);
                        lugar.setTelefono(telefono);
                        lugar.setDescripcionCorta(descripcion);
                        lugar.setHorario("(No disponible)");

                        LatLng loc=new LatLng(latitud,longitud);
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.markgeneral);
                        if ( categoria.toLowerCase().contains(" bar") || categoria.toLowerCase().contains("cantina")
                                || categoria.toLowerCase().contains("cerve") || categoria.toLowerCase().contains("mezc")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markbar);
                        }else if (categoria.toLowerCase().contains("hotel")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markhotel);
                        }else if (categoria.toLowerCase().contains("restaurant")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markrestaurant);
                        }else if (categoria.toLowerCase().contains("medic")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markfarmacia);
                        }else if (categoria.toLowerCase().contains("hospi")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markhospital);
                        }else if (categoria.toLowerCase().contains("flor")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markfloreria);
                        }else if (categoria.toLowerCase().contains("cafe")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markcafe);
                        } else if (categoria.toLowerCase().contains("comercio")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markcomercial);
                        }else if (categoria.toLowerCase().contains("religio")){
                            icon = BitmapDescriptorFactory.fromResource(R.drawable.markiglesia);
                        }

                        if (markerHashMap.get(id)!=null){
                            //Ya estaba
                        }else {
                            //añadir
                            Marker marker=mMap.addMarker(new MarkerOptions()
                                    .position(loc)
                                    .icon(icon));
                            marker.setTag(id);
                            markerHashMap.put(id,marker);
                        }

                        //falta agregar al builder de vista

                        if (realm!=null){
                            if (realm.isClosed() || realm.isEmpty()) {
                                realm=null;
                                RealmConfiguration config = new RealmConfiguration
                                        .Builder()
                                        .deleteRealmIfMigrationNeeded()
                                        .build();

                                Realm.setDefaultConfiguration(config);

                                realm = Realm.getDefaultInstance();
                                realm.setAutoRefresh(true);
                            }
                        }else {
                            RealmConfiguration config = new RealmConfiguration
                                    .Builder()
                                    .deleteRealmIfMigrationNeeded()
                                    .build();

                            Realm.setDefaultConfiguration(config);

                            realm = Realm.getDefaultInstance();
                            realm.setAutoRefresh(true);
                        }

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                // This will create a new object in Realm or throw an exception if the
                                // object already exists (same primary key)
                                // realm.copyToRealm(obj);

                                // This will update an existing object with the same primary key
                                // or create a new one if the primary key doesn't exists
                                realm.copyToRealmOrUpdate(lugar);
                            }
                        });

                    }
                    realm.close();

                    //sonido


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (progressDialog!=null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Log.e("dismiss in", "denue after");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"NearPlaces:"+" statusCode:"+statusCode);
                if (progressDialog!=null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Log.e("dismiss in", "denue fail");
                }
            }
        });
    }

    private void vioAlerta(){

        Log.e(LOG_TAG, "get before U" + idUnidad + " C" +idConductor);

        double lat=currentLocation.getLatitude();
        double lng=currentLocation.getLongitude();

        AsyncHttpClient cliente=new AsyncHttpClient();
        String url=getString(R.string.mainURL)+"/Conductor/alerta_revisada";
        RequestParams params=new RequestParams();
        params.put("idConductor",idConductor);
        params.put("dLatitud",lat);
        params.put("dLongitud",lng);

        cliente.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.e(LOG_TAG, "get resp " + response);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG, "get statusCode" + statusCode);
                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión " + statusCode + ".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                vioAlerta();
            }
        });
    }

    private void panicRequest(){
        Log.e(LOG_TAG, "PANIC get before U" + idUnidad + " C" +idConductor);

        double lat=currentLocation.getLatitude();
        double lng=currentLocation.getLongitude();

        AsyncHttpClient cliente=new AsyncHttpClient();
        String url=getString(R.string.mainURL)+"/Conductor/agregar_alerta";
        RequestParams params=new RequestParams();
        params.put("idConductor",idConductor);
        params.put("dLatitud",lat);
        params.put("dLongitud",lng);

        cliente.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.e(LOG_TAG, "get resp " + response);

                Snackbar.make(getWindow().getDecorView().getRootView(), "Enviado.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                panic.setEnabled(false);
                panic.setColorFilter(Color.GRAY);

                //Se puede repetir la solicitud de panico?
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG, "get statusCode" + statusCode);
                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión " + statusCode + ".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void statusLibre(){
        //TODO:
        //Se actualiza la interfaz y las preferencias para que el usuario este disponible para nuevos servicios

        if(calificarEfectivo!=null && calificarEfectivo.isShowing()){
            calificarEfectivo.dismiss();
            calificarEfectivo=null;
        }

        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(getString(R.string.current_service));
        editor.remove(getString(R.string.current_status));
        editor.remove(getString(R.string.current_confirmado));
        editor.remove(getString(R.string.current_cancelado));
        editor.remove(getString(R.string.current_cancelado_v));
        editor.remove(getString(R.string.current_llego_origen));
        editor.remove(getString(R.string.current_calificado));
        editor.remove(getString(R.string.current_costo_conductor));
        editor.remove(getString(R.string.current_costo_pp));
        editor.remove(getString(R.string.current_costo_sugerido));
        editor.remove(getString(R.string.current_costo_usuario));
        editor.putInt(getString(R.string.current_ocupado),0);
        editor.commit();

        currentCancelado=0;
        //currentCanceladoV=0;
        currentCostoC=0;
        currentCostoU=0;
        currentCostoS=0;
        currentCostoPP=0;
        currentCalificado=0;
        currentStatus=0;
        currentService=0;
        orig=null;
        dest=null;

        if (mOrigen!=null){
            mOrigen.remove();
        }
        mOrigen=null;
        if (mDestino!=null){
            mDestino.remove();
        }
        mDestino=null;
        if (mUsuario!=null){
            mUsuario.remove();
        }
        mUsuario=null;
        if (polyline!=null){
            polyline.remove();
        }
        polyline=null;

        //falta : polilineas de ruta sugerida y tiempo estimado

        llego.setEnabled(false);
        llego.setVisibility(View.GONE);

        bStatus.setText("LIBRE");
        bStatus.setTextColor(Color.WHITE);
        bStatus.setBackgroundColor(Color.parseColor("#333333"));
        bStatus.setVisibility(View.VISIBLE);
        bCancelar.setEnabled(false);
        bCancelar.setVisibility(View.GONE);
        estado.setEnabled(false);
        estado.setVisibility(View.GONE);
        llego.setEnabled(false);
        llego.setVisibility(View.GONE);
        navig.setEnabled(false);
        navig.setVisibility(View.GONE);

        mMap.clear();

        if (currentRealmService!=null){
            if (realm!=null && realm.isClosed()){
                RealmConfiguration config = new RealmConfiguration
                        .Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build();

                Realm.setDefaultConfiguration(config);

                realm=Realm.getDefaultInstance();
                realm.setAutoRefresh(true);
            }

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // This will create a new object in Realm or throw an exception if the
                    // object already exists (same primary key)
                    // realm.copyToRealm(obj);

                    // This will update an existing object with the same primary key
                    // or create a new one if the primary key doesn't exists
                    realm.copyToRealmOrUpdate(currentRealmService);
                }
            });
        }

        currentRealmService=null;

        if (realm!=null && !realm.isClosed()){
            realm.close();
        }

        navigationMenu=navigationView.getMenu();

        navigationMenu.findItem(R.id.nav_servicios).setTitle("Serivicios disponibles");


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //replaces the default 'Back' button action

        if(keyCode==KeyEvent.KEYCODE_BACK)   {
            // something here
            finish();
        }
        return true;
    }

    private void bStatusAction(){
        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);
        if (preferences.contains(getString(R.string.current_status))){
            currentCancelado=preferences.getInt(getString(R.string.current_cancelado),0);
            currentCanceladoV=preferences.getInt(getString(R.string.current_cancelado_v),0);
            currentStatus=preferences.getInt(getString(R.string.current_status),0);

            if (currentStatus==2){
                //Confirmar subida del pasajero
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("¿Confirmar subida del pasajero?")
                        .setCancelable(true)
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Confirmar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (currentRealmService!=null) {
                                            dest = new LatLng(currentRealmService.getLat_d(), currentRealmService.getLng_d());
                                        }

                                        if (currentLatLng != null && dest != null) {
                                            new DownloadTask().execute(new DirectionsUrl().getUrl(currentLatLng, dest));
                                        }

                                        requestChangeStatus(3);
                                        dialog.dismiss();
                                    }
                                }).show();
            }

            if (currentStatus==3){
                if (currentAcreditadoPP==2) {
                        //confirmar bajada
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("¿Confirmar bajada del pasajero?")
                                .setCancelable(true)
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setPositiveButton("Confirmar",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                requestChangeStatus(4);
                                                dialog.dismiss();
                                            }
                                        }).show();
                    }else {
                        //El cliente no ha pagado, por favor cobrar en efectivo
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(Html.fromHtml("<font color='#000000'>¡Pago pendiete!</font>"))
                                .setMessage("No se ha acreditado ningún pago electrónico, cobrar en efectivo.\n¿Confirmar bajada del pasajero?")
                                .setCancelable(true)
                                .setNegativeButton("No, cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setPositiveButton("Si, continuar",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //Dialog evaluacion e ingrese monto cobrado
                                                requestChangeStatus(4);
                                                dialog.dismiss();
                                            }
                                        }).show();
                    }
            }
        }else {
            currentOcupado=preferences.getInt(getString(R.string.current_ocupado),0);
            if (currentOcupado==1){
                requestChangeStatus(0);
            }else {
                requestChangeStatus(1);
            }
        }
    }

    private void getCurrentStatus(){
        /** TODO:
         *
         * Retorna un json con la siguiente información:
         *
         * retorna cuantas alertas existen
         *
         * servicioActivo es un status que indica si existe un servicio propio actualmente,
         * si es 1 extraigo el json datosServicio y creo o actualizo la base de datos.
         *
         * login es un satus que indica si la sesion actual es valida o no,
         * 1 es sesion valida, 0 es sesion invalida (cerrar sesion).
         */

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        final String[] login = new String[1];
        final String[] servicioActivo = new String[1];
        final int[] alertas = new int[1];
        final JSONObject[] jsonObject = new JSONObject[1];
        final String[] saldo = new String[1];
        final int[] ocupado = new int[1];


        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            Log.e("dismiss in","c stat before");
        }
        progressDialog=null;
        if(MainActivity.this.getParent()!=null){
            progressDialog=new ProgressDialog(MainActivity.this.getParent());
        }else{
            progressDialog=new ProgressDialog(MainActivity.this);
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //if is first time not cancellable
        if (isFirstTime || !currentValid){
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading...");
            if(!MainActivity.this.isFinishing()) {
                progressDialog.show();
                Log.e("show in","get c stat");
            }
        }

        Log.e(LOG_TAG," getCurrentStatus before "+ idConductor);

        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        String url = getString(R.string.mainURL)+"/Conductor/inicia_principal_conductor"; //_CONDUCTOR?
        parametros.put("idConductor",idConductor);
        //Mandar token prob.
        cliente.setConnectTimeout(20000);
        cliente.setTimeout(40000);
        cliente.post(url, parametros, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG," getCurrentStatus response:"+ new String(responseBody));
                currentValid=true;
                try {
                    jsonObject[0] =new JSONObject(new String(responseBody));
                    login[0] = jsonObject[0].getString("login");
                    servicioActivo[0] = jsonObject[0].getString("servicioActivo");
                    saldo[0] = jsonObject[0].getString("dSaldoConductor");
                    ocupado[0] = jsonObject[0].getInt("lOcupado");
                    alertas[0] = jsonObject[0].getInt("alertas");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Se guardan en preferencias el saldo y el status de ocupado
                float saldoPref = Float.parseFloat(saldo[0]);
                SharedPreferences.Editor editorb=preferences.edit();
                editorb.putFloat(getString(R.string.current_saldo),saldoPref);
                editorb.putInt(getString(R.string.current_ocupado),ocupado[0]);
                editorb.commit();


                if (login[0].contains("0")){
                    Log.e(LOG_TAG," getCurrentStatus login 0");
                    if (dialogiIniciaSesion!=null && dialogiIniciaSesion.isShowing()){
                        dialogiIniciaSesion.dismiss();
                    }
                    dialogiIniciaSesion=null;
                    dialogiIniciaSesion=new AlertDialog.Builder(MainActivity.this)
                            .setTitle("¡UPS!")
                            .setMessage("Parece que tienes algunos problemas con tu usuario. Por favor comunicate con tu administrador.") //pasar a strings
                            //Agregar codigo de error
                            .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mGoogleApiClient!=null){
                                        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                                        }
                                    }

                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.remove(getString(R.string.p_id_unidad));
                                    editor.remove(getString(R.string.p_id_conductor));
                                    editor.remove(getString(R.string.p_username));
                                    editor.remove(getString(R.string.p_pass));
                                    editor.remove(getString(R.string.p_nick));
                                    editor.remove("active");
                                    editor.commit();

                                    shouldEnd = true;

                                    dialog.dismiss();
                                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intentm);
                                    finish();
                                }
                            })
                            .show();
                }

                if (servicioActivo[0].contains("1")){
                    Log.e(LOG_TAG," getCurrentStatus servicioActivo 1");

                    try{
                        Log.e(LOG_TAG," getCurrentStatus servicioActivo 1 inside try");
                        JSONObject jsonServicio= jsonObject[0].getJSONObject("datosServicio");
                        //Fill data
                        int id;
                        String ap_mat;
                        String ap_pat;
                        String nombre;
                        int cancelado;
                        int cancelado_v;
                        int confirmado;
                        int año;
                        String foto_c;
                        String foto_un;
                        String email;
                        String fecha;
                        String modelo;
                        String marca;
                        String placas;
                        double lat_d;
                        double lng_d;
                        double lat_o;
                        double lng_o;
                        float calif=0;
                        String referencia;
                        String dir_dest;
                        String dir_orig;
                        String col_dest;
                        String col_orig;
                        int status;
                        String tel;
                        int costo_s=0;
                        int costo_u=0;
                        int costo_c=0;
                        int ppCosto=0,ppSolicitado=0,ppAcreditado=0;

                        //TODO: Importante
                        //Se necesitan todos los datos del servicio aqui: si esta cancelado, fecha, status de pago, de confirmaciones, etc.
                        //El usuario pudo haber borrado historial, cache de la app o ocurrido cualquer perdida de información.
                        //Las notificaciones push que se hayan mandado anteriormente no se repetiran y pudo perderse esa información.

                        //Si esxta cancelado no se puede mandar la informacion, "ya no esta",
                        // pero, se puede hacer algo para que solo regrese la informacion solo a la siguiente consulta?
                        // con eso seria suficiente

                        //lConfirmadoConductor siempre 1

                        id=         jsonServicio.getInt("idServicio");
                        nombre=     jsonServicio.getString("aNombreUsuario");
                        //cancelado=  jsonServicio.getInt("lCancelado");
                        //cancelado_v=jsonServicio.getInt("");
                        email=      jsonServicio.getString("aEmail");
                        fecha=      jsonServicio.getString("fFechaHoraSolicitud");
                        lat_d=      jsonServicio.getDouble("dLatitudDestino");
                        lng_d=      jsonServicio.getDouble("dLongitudDestino");
                        lat_o=      jsonServicio.getDouble("dLatitudOrigen");
                        lng_o=      jsonServicio.getDouble("dLongitudOrigen");
                        referencia= jsonServicio.getString("aReferencia");
                        dir_dest=   jsonServicio.getString("aDireccionDestino");
                        dir_orig=   jsonServicio.getString("aDireccionOrigen");
                        col_dest=   jsonServicio.getString("aColoniaDestino");
                        col_orig=   jsonServicio.getString("aColoniaOrigen");
                        status=     jsonServicio.getInt("idStatus");
                        tel=        jsonServicio.getString("aTelefono");
                        costo_s=    jsonServicio.getInt("dCostoSugerido");
                        costo_u=    jsonServicio.getInt("dCostoUsuario");
                        double costo_ccc=    jsonServicio.getDouble("dCostoConductor");
                        costo_c=(int)costo_ccc;
                        Log.e("costo_c",costo_c+" ccc "+costo_ccc);
                        if (jsonServicio.isNull("dCalificacionServicio")){
                            calif=0;
                        }else {
                            calif=      jsonServicio.getInt("dCalificacionUsuario");//a
                        }

                        ppCosto=        jsonServicio.getInt("dCostoPaypal");
                        ppAcreditado=   jsonServicio.getInt("idStatusPaypal"); //2 completado //1 fallo
                        ppSolicitado=   jsonServicio.getInt("lSolicitudPaypal");

                        final Servicio servicio = new Servicio();
                        servicio.setId(id);
                        servicio.setNombre(nombre);
                        //servicio.setCancelado(cancelado);
                        //servicio.setCancelado_v(cancelado_v);
                        servicio.setConfirmado(1);

                        servicio.setEmail(email);
                        servicio.setFecha(fecha);

                        servicio.setLat_d(lat_d);
                        servicio.setLng_d(lng_d);
                        servicio.setLat_o(lat_o);
                        servicio.setLng_o(lng_o);
                        servicio.setReferencia(referencia);
                        servicio.setDir_dest(dir_dest);
                        servicio.setDir_orig(dir_orig);
                        servicio.setColonia_dest(col_dest);
                        servicio.setColonia_orig(col_orig);
                        servicio.setStatus(status);
                        servicio.setTel(tel);
                        servicio.setCostoSug(costo_s);
                        servicio.setCostoUs(costo_u);
                        servicio.setCostoCond(costo_c);
                        servicio.setCalif_user(calif);

                        if (realm!=null && realm.isClosed()){
                            RealmConfiguration config = new RealmConfiguration
                                    .Builder()
                                    .deleteRealmIfMigrationNeeded()
                                    .build();

                            Realm.setDefaultConfiguration(config);

                            realm=Realm.getDefaultInstance();
                            realm.setAutoRefresh(true);
                        }

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                // This will create a new object in Realm or throw an exception if the
                                // object already exists (same primary key)
                                // realm.copyToRealm(obj);

                                // This will update an existing object with the same primary key
                                // or create a new one if the primary key doesn't exists
                                realm.copyToRealmOrUpdate(servicio);
                            }
                        });

                        currentRealmService=servicio;

                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putInt(getString(R.string.current_service),id);
                        editor.putInt(getString(R.string.current_status),status);
                        editor.putInt(getString(R.string.current_confirmado),1);
                        editor.putInt(getString(R.string.current_costo_conductor),costo_c);
                        editor.putInt(getString(R.string.current_costo_usuario),costo_u);
                        editor.putInt(getString(R.string.current_costo_sugerido),costo_s);
                        editor.putFloat(getString(R.string.current_calificado),calif);
                        editor.putInt(getString(R.string.current_acreditado_pp),ppAcreditado);
                        editor.putInt(getString(R.string.current_costo_pp),ppCosto);
                        editor.putInt(getString(R.string.current_solicitado_pp),ppSolicitado);

                        editor.putInt(getString(R.string.current_ocupado),0);

                        editor.commit();

                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        Log.e(LOG_TAG," getCurrentStatus servicioActivo 1 catch e: "+e.toString());
                        e.printStackTrace();
                    }

                    servicioEnCurso();

                }else if (servicioActivo[0].contains("0")){
                    Log.e(LOG_TAG," getCurrentStatus servicioActivo 0");

                    currentStatus=preferences.getInt(getString(R.string.current_status),0);
                    currentCalificado=preferences.getFloat(getString(R.string.current_calificado),0);

                    if (currentStatus==4 && currentCalificado==0){
                        servicioEnCurso();
                    }else {
                        if (currentStatus<4){
                            //Falta fue cancelado
                            statusLibre();
                        }else {
                            if (preferences.contains(getString(R.string.current_ocupado))){
                                currentOcupado=preferences.getInt(getString(R.string.current_ocupado),0);
                                if (currentOcupado==0){
                                    statusLibre();
                                }else {
                                    bStatus.setText("OCUPADO");
                                    bStatus.setTextColor(Color.BLACK);
                                    bStatus.setBackgroundColor(Color.parseColor("#ffc107"));
                                    bStatus.setVisibility(View.VISIBLE);

                                    estado.setEnabled(false);
                                    estado.setVisibility(View.GONE);
                                }
                            }else {
                                statusLibre();
                            }
                        }
                    }

                }

                if (alertas[0]!=0){
                    Log.e(LOG_TAG," getCurrentStatus alertas not 0");

                    final int[] id = new int[1];
                    final String[] imageC = new String[1];
                    final String[] imageU = new String[1];
                    final String[] modelo = new String[1];
                    final String[] marca = new String[1];
                    final String[] placas = new String[1];
                    final String[] nombre = new String[1];
                    final String[] nombre2 = new String[1];
                    final String[] nombre3 = new String[1];
                    final String[] fecha = new String[1];
                    final double[] lat =new double[1];
                    final double[] lng =new double[1];

                    final JSONArray[] jsonArray = new JSONArray[1];
                    try {
                        jsonArray[0]=new JSONArray(new String(responseBody));
                        for (int x=0;x<jsonArray[0].length();x++){
                            Log.e("For", x+ " lenght " + jsonArray[0].length());


                            id[0]=jsonArray[0].getJSONObject(x).getInt("idPanico");
                            imageC[0]=jsonArray[0].getJSONObject(x).getString("aFotografia");
                            imageU[0]=jsonArray[0].getJSONObject(x).getString("aFotografiaUnidad");
                            modelo[0]=jsonArray[0].getJSONObject(x).getString("aModelo");
                             marca[0]=jsonArray[0].getJSONObject(x).getString("aMarca");
                            placas[0]=jsonArray[0].getJSONObject(x).getString("aPlacas");
                             nombre[0]=jsonArray[0].getJSONObject(x).getString("aNombreConductor");
                            nombre2[0]=jsonArray[0].getJSONObject(x).getString("aApellidoPatConductor");
                            nombre3[0]=jsonArray[0].getJSONObject(x).getString("aApellidoMatConductor");
                             fecha[0]=jsonArray[0].getJSONObject(x).getString("fFechaHoraAltaAlerta");
                            lat[0]=jsonArray[0].getJSONObject(x).getDouble("dLatitudAlerta");
                            lng[0]=jsonArray[0].getJSONObject(x).getDouble("dLongitudAlerta");

                            String nom=nombre[0]+" "+nombre2[0]+" "+nombre3[0];

                            final Panico panico =new Panico();

                            panico.setIDalerta(id[0]);
                            panico.setImagenC(imageC[0]);
                            panico.setImagenU(imageU[0]);
                            panico.setModelo(modelo[0]);
                            panico.setMarca(marca[0]);
                            panico.setPlacas(placas[0]);
                            panico.setNombre(nom);
                            panico.setFecha(fecha[0]);
                            panico.setLatitud(lat[0]);
                            panico.setLongitud(lng[0]);

                            numAlertas=jsonArray.length;
                            int numAP=preferences.getInt(getString(R.string.current_alertas),0);

                            if (realm!=null && realm.isClosed()){
                                RealmConfiguration config = new RealmConfiguration
                                        .Builder()
                                        .deleteRealmIfMigrationNeeded()
                                        .build();

                                Realm.setDefaultConfiguration(config);

                                realm=Realm.getDefaultInstance();
                                realm.setAutoRefresh(true);
                            }

                            if (numAP!=numAlertas){
                                final RealmResults<Panico> results = realm.where(Panico.class).findAll();

                                // All changes to data must happen in a transaction
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        results.deleteAllFromRealm();
                                    }
                                });
                            }

                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    // This will create a new object in Realm or throw an exception if the
                                    // object already exists (same primary key)
                                    // realm.copyToRealm(obj);

                                    // This will update an existing object with the same primary key
                                    // or create a new one if the primary key doesn't exists
                                    realm.copyToRealmOrUpdate(panico);
                                }
                            });


                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    int numAP=preferences.getInt(getString(R.string.current_alertas),0);

                    if (numAlertas<numAP){
                        int diff=numAP-numAlertas;

                        if (diff==1){
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Hay una alerta nueva.", Snackbar.LENGTH_LONG)
                                    .setAction("Ver ", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intentAl = new Intent(MainActivity.this, EmergenciasActivity.class);
                                            startActivity(intentAl);
                                        }
                                    }).show();
                        }else {
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Hay "+diff+" alertas nuevas.", Snackbar.LENGTH_LONG)
                                    .setAction("Ver ", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intentAl = new Intent(MainActivity.this, EmergenciasActivity.class);
                                            startActivity(intentAl);
                                        }
                                    }).show();
                        }
                        //Inform new alerts
                    }

                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putInt(getString(R.string.current_alertas),numAlertas);
                    editor.commit();


                }else {
                    Log.e(LOG_TAG," getCurrentStatus alertas 0");
                    numAlertas=0;
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putInt(getString(R.string.current_alertas),numAlertas);
                    editor.commit();
                }

                if ( progressDialog.isShowing() && (!isFirstTime || currentStatus>=1 || shouldEnd)) {
                    progressDialog.dismiss();
                    Log.e("dismiss in", "getCurrentStatus last");
                }else {
                    Log.e("error location", "currentLction="+currentLocation+" api="+mGoogleApiClient.isConnected()+" firsttime="+isFirstTime);
                    wentSettings=true;
                    if (currentLocation==null){
                        if (mGoogleApiClient.isConnected()){
                            mGoogleApiClient.disconnect();
                            Log.e("error location", "API Dissconected 1");
                        }
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"getCurrentStatus statusCode"+statusCode);
                //TODO: Importante
                //Fallo al consutlar el status actual, la funcionalidad de la app esta comprometida.
                //Ejemplo: el conductor podria recibir o aceptar un nuevo servicio que resultaria en errores e incosistencias
                //Comprobar currentValid antes de cada accion
                currentValid=false;
                progressDialog.dismiss();
                Log.e("dismiss in", "getCurrentStaus fail");
                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nIntentando consultar el status actual.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void servicioEnCurso(){
        //TODO:
        //Llamar este metodo en cada cambio de status o caso de uso, desde webservices o pushs.
        //Analizar cada posible caso de combinaciones y su acción.

        //falta : sonidos

        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();//not running
            Log.e("dismiss in","reuqestpayment before");
        }
        progressDialog=null;
        if(MainActivity.this.getParent()!=null){
            progressDialog=new ProgressDialog(MainActivity.this.getParent());
        }else{
            progressDialog=new ProgressDialog(MainActivity.this);
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        if(!MainActivity.this.isFinishing()) {
            progressDialog.show();
            Log.e("show in","");
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        navigationMenu=navigationView.getMenu();

        navigationMenu.findItem(R.id.nav_servicios).setTitle("Datos del servicio");

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);
        currentCancelado=preferences.getInt(getString(R.string.current_cancelado),0);
        currentCanceladoV=preferences.getInt(getString(R.string.current_cancelado_v),0);
        currentCostoC=preferences.getInt(getString(R.string.current_costo_conductor),0);
        currentCostoU=preferences.getInt(getString(R.string.current_costo_usuario),0);
        currentCostoS=preferences.getInt(getString(R.string.current_costo_sugerido),0);
        currentCalificado=preferences.getFloat(getString(R.string.current_calificado),0);
        currentStatus=preferences.getInt(getString(R.string.current_status),0);
        currentService=preferences.getInt(getString(R.string.current_service),0);
        currentLlegoOrigen=preferences.getInt(getString(R.string.current_llego_origen),0);
        currentCostoPP=preferences.getInt(getString(R.string.current_costo_pp),0);
        currentAcreditadoPP=preferences.getInt(getString(R.string.current_acreditado_pp),0);
        currentSolicitadoPP=preferences.getInt(getString(R.string.current_solicitado_pp),0);

        Log.e("costoC",""+currentCostoC);
        if (costoIPP!=null) {
            costoIPP.setText("" + currentCostoC);
        }
        if (costoE!=null) {
            costoE.setText("" + currentCostoC);
        }

        if (currentSolicitadoPP>0 && currentCostoPP<10){
            Log.e("wtf servEnCur()","solicitadoPP in");

            ingresaCostoPP.show();
            //ingresaCostoPP.dismiss();

        }else {
            Log.e("wtf servEnCur()","solicitadoPP else");
            ingresaCostoPP.dismiss();
            progressDialog.dismiss();
            Log.e("dismiss in", "servEnCur 1");
        }

        if (realm==null){
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);

            realm=Realm.getDefaultInstance();
            realm.setAutoRefresh(true);
        }
        if (realm!=null && realm.isClosed()){
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);

            realm=Realm.getDefaultInstance();
            realm.setAutoRefresh(true);
        }

        if (currentRealmService==null){
            // All changes to data must happen in a transaction
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    currentRealmService = realm.where(Servicio.class).equalTo("id",currentService).findFirst();
                }
            });
        }

        if (currentCancelado==1){
            //TODO:
            //Limpiar interfaz del servicio, guardar en historial como cancelado
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    currentRealmService.setCancelado(1);
                }
            });

            statusLibre();
            if (currentCanceladoV==0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Tu servicio ha sido cancelado.")
                        .setTitle(Html.fromHtml("<font color='#000000'>:I</font>"))
                        .setCancelable(false)
                        .setNeutralButton("Aceptar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SharedPreferences.Editor editor=preferences.edit();
                                        editor.remove(getString(R.string.current_cancelado_v));
                                        editor.commit();
                                        currentCanceladoV=0;
                                        dialog.dismiss();
                                    }
                                }).show();
            }
        }else {

            bCancelar.setEnabled(true);
            bCancelar.setVisibility(View.VISIBLE);

            try{

            LatLng permanentDest = null;

            permanentDest = new LatLng(currentRealmService.getLat_d(), currentRealmService.getLng_d());

            if (dest == null) {

                if (currentStatus == 2) {

                    dest = new LatLng(currentRealmService.getLat_o(), currentRealmService.getLng_o());

                } else {

                    dest = new LatLng(currentRealmService.getLat_d(), currentRealmService.getLng_d());

                }

            }
            if (orig == null) {

                orig = new LatLng(currentRealmService.getLat_o(), currentRealmService.getLng_o());

            }

            if (mDestino == null || !mDestino.isVisible()) {
                mDestino = mMap.addMarker(new MarkerOptions()
                        .position(permanentDest)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerb))
                        .title("Destino"));
            }
            if (mOrigen == null || !mOrigen.isVisible()) {
                if (orig != null) {
                    mOrigen = mMap.addMarker(new MarkerOptions()
                            .position(orig)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.markera))
                            .title("Origen"));
                }
            }

        }catch (Exception ignored){}
            //falta : que el origen sea la ubicacion dinamica del usuario, y actualizar su marker

            Log.e("wtf serEnCur()"," solicitado:"+currentSolicitadoPP+" costoPP:"+currentCostoPP);

            if (currentAcreditadoPP>0) {
                Log.e("wtf servEnCur()","acreditadoPP in");
                Snackbar.make(getWindow().getDecorView().getRootView(), "El pago del servicio actual esta acreditado", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }else {
                Log.e("wtf servEnCur()","acreditadoPP else in");
            }

            if (currentStatus==2){
                Log.e("wtf servEnCur()","status2 in");


                if(count2<=1){
                    count2=count2+1;

                    //Post para "ingresar" el costo de PP
                    AsyncHttpClient cliente = new AsyncHttpClient();
                    RequestParams parametros = new RequestParams();
                    String url = getString(R.string.mainURL)+"/Servicios/conductor_costo_paypal";
                    parametros.put("idServicio", currentService);
                    parametros.put("dCostoPaypal", currentCostoC);

                    Log.e(LOG_TAG," montoPP prePost idServicio "+currentService +" costoPP "+costoIPP.getText().toString().trim());

                    cliente.post(url, parametros, new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String resp=new String(responseBody);
                            Log.e(LOG_TAG,"costoPP success"+ resp);


                            currentCostoPP = Integer.parseInt(costoIPP.getText().toString().trim());
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putInt(getString(R.string.current_acreditado_pp),0);
                            editor.putInt(getString(R.string.current_costo_pp),Integer.parseInt(costoIPP.getText().toString().trim()));
                            editor.putInt(getString(R.string.current_solicitado_pp),1);
                            editor.commit();

                            ingresaCostoPP.dismiss();
                            ingresaCostoPP.cancel();
                            progressDialog.dismiss();
                            Log.e("dismiss in", "costoPP 22");

                            Log.e(LOG_TAG, "From 6");
                            getCurrentStatus();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            progressDialog.dismiss();
                            Log.e("dismiss in", "costoPP 22 fail");
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                }

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        currentRealmService.setStatus(2);
                    }
                });
                bStatus.setText("Subida del pasajero");
                bStatus.setTextColor(Color.WHITE);
                bStatus.setBackgroundColor(Color.parseColor("#333333"));
                bStatus.setVisibility(View.VISIBLE);

            }

            if (currentStatus<=2){

                navig.setEnabled(true);
                navig.setVisibility(View.VISIBLE);

                if (currentLlegoOrigen>0){
                    llego.setEnabled(false);
                    llego.setVisibility(View.GONE);
                }else {
                    llego.setEnabled(true);
                    llego.setVisibility(View.VISIBLE);
                }
            }else {
                llego.setEnabled(false);
                llego.setVisibility(View.GONE);
            }

            if (currentStatus==3){
                Log.e("wtf servEnCur()","status3 in");
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        currentRealmService.setStatus(3);
                    }
                });
                bStatus.setText("Bajada del pasajero");
                bStatus.setTextColor(Color.WHITE);
                bStatus.setBackgroundColor(Color.parseColor("#333333"));
                bStatus.setVisibility(View.VISIBLE);
            }

            if (currentStatus==4){
                Log.e("wtf servEnCur()","status4 in");
                orig=null;
                dest=null;
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        currentRealmService.setStatus(4);
                    }
                });

                if (currentCalificado==0 || currentCostoC<10) {
                    Log.e("wtf servEnCur()","not calif in");
                    if (currentAcreditadoPP==2) {
                        Log.e("wtf servEnCur()","pagado pp");
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                currentRealmService.setTipo_pago("PayPal");
                            }
                        });

                        calificarServicio("pagado");
                    }else {
                        Log.e("wtf servEnCur()","cobro efctivo");
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                currentRealmService.setTipo_pago("Efectivo");
                            }
                        });

                        calificarServicio("efectivo");
                    }

                }

            }

        }

    }

    private void paymentAfter(int idServicio){
        //Extraer objeto realm de ese servicio, mostrar al usuario sus datos y pedir
        //que ingrese una cantidad



    }

    private void getMyUser(){

        final JSONObject[] usuarioData = new JSONObject[1];


        Log.e(LOG_TAG," getMyUser before "+ idConductor);

        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        String url = getString(R.string.mainURL)+"/Conductor/ubicacion_usuario";
        parametros.put("idConductor",idConductor);
        //Mandar token prob.
        cliente.post(url, parametros, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG," getMyUser response:"+ new String(responseBody));
                currentValid=true;

                try {
                    usuarioData[0] = new JSONObject(new String(responseBody));

                            //Agregar cada unidad en forma de marker al mapa

                            int id;
                            double lat;
                            double lng;

                            //id=    usuarioData[0].getInt("idServicio");
                            lat=   usuarioData[0].getDouble("dLatitudRegistro");
                            lng=   usuarioData[0].getDouble("dLongitudRegistro");

                            LatLng currentLU=new LatLng(lat,lng);

                            if (mUsuario==null){
                                        try{
                                            mUsuario=mMap.addMarker(new MarkerOptions()
                                                    .position(currentLU)
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.musuario))
                                                    .title(currentRealmService.getNombre()));
                                        }catch (Exception ignored){

                                        }

                            }else {
                                //Si ya se encontraba en el mapa
                                //Obtenemos el marker y lo animamos a la nueva locacion

                                LatLng ultima=mUsuario.getPosition();
                                LatLngInterpolator interpolator = new LatLngInterpolator.Spherical();
                                interpolator.interpolate(10,ultima,currentLU);
                                MarkerAnimation.animateMarkerToICS(mUsuario,currentLU,interpolator);
                            }


                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"getMyUser statusCode"+statusCode);
                //TODO: Importante
                //Fallo al consutlar el status actual, la funcionalidad de la app esta comprometida.
                //Ejemplo: el conductor podria recibir o aceptar un nuevo servicio que resultaria en errores e incosistencias
                //Comprobar currentValid antes de cada accion

                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nIntentando consultar el status actual.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    private void calificarServicio(String type){
        Log.e("wtf califServ()","in");
        if (type.contains("efectivo")){
            Log.e("wtf servEnCur()","efectivo in");
            //showefectivo
            calificarEfectivo.show();
        }else {
            Log.e("wtf servEnCur()","electronico in");
            //showelectronico
            calificarElectronico.show();

        }
    }

    private void requestCancelarServ(){
        if (progressDialog!=null && progressDialog.isShowing()) {             progressDialog.dismiss();             Log.e("dismiss in","c stat before");         }          progressDialog=null;          if(MainActivity.this.getParent()!=null){             progressDialog=new ProgressDialog(MainActivity.this.getParent());         }else{             progressDialog=new ProgressDialog(MainActivity.this);         }          progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);         progressDialog.setCancelable(false);         progressDialog.setTitle("Loading...");          if(!MainActivity.this.isFinishing()) {             progressDialog.show();             Log.e("show in","");         }

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        String url;

        //Llegada a origen
        if (currentService!=0) {
            url = getString(R.string.mainURL) + "/Conductor/cancelar_servicio";
            parametros.put("idConductor", idConductor);
            parametros.put("idServicio", currentService);
            cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    Log.e(LOG_TAG, "requestCancelar " + response);
                    statusLibre();
                    progressDialog.dismiss();
                    Log.e("dismiss in", "rc");
                    Toast.makeText(MainActivity.this, "Servicio cancelado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(LOG_TAG, "requestCancelar statusCode " + statusCode);
                    progressDialog.dismiss();
                    Log.e("dismiss in", "rc fail");
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión " + statusCode + ".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }else {
            Log.e(LOG_TAG, "requestCancelar  currentService=0");
            progressDialog.dismiss();
            Log.e("dismiss in", "rc error 0");
            Snackbar.make(getWindow().getDecorView().getRootView(), "Error 404.\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void requestChangeStatus(final int type){
        //TODO:
        //Webservices para cambiar status:
        //Libre, Ocupado, llegada a origen, subida de pasajero, bajada de pasajero

        if (progressDialog!=null && progressDialog.isShowing()) {             progressDialog.dismiss();             Log.e("dismiss in","c stat before");         }          progressDialog=null;          if(MainActivity.this.getParent()!=null){             progressDialog=new ProgressDialog(MainActivity.this.getParent());         }else{             progressDialog=new ProgressDialog(MainActivity.this);         }          progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);         progressDialog.setCancelable(false);         progressDialog.setTitle("Loading...");          if(!MainActivity.this.isFinishing()) {             progressDialog.show();             Log.e("show in","");         }

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        String url;

        switch (type){
            case 0:
                //Libre
                url=getString(R.string.mainURL) +"/Conductor/desocupar_unidad";
                parametros.put("idConductor",idConductor);
                parametros.put("idUnidad",idUnidad);
                ;
                cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response=new String(responseBody);
                        Log.e(LOG_TAG,"changeStatus "+ type + " "+response);
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putInt(getString(R.string.current_ocupado),0);
                        editor.commit();
                        statusLibre();
                        progressDialog.dismiss();
                        Log.e("dismiss in", "type 0");
                        Toast.makeText(MainActivity.this,"Status cambiado correctamente",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(LOG_TAG,"changeStatus "+ type + " statusCode"+statusCode);
                        progressDialog.dismiss();
                        Log.e("dismiss in", "type 0 fail");
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                break;
            case 1:
                //Ocupado
                url=getString(R.string.mainURL) +"/Conductor/ocupar_unidad";
                parametros.put("idConductor",idConductor);
                parametros.put("idUnidad",idUnidad);
                cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response=new String(responseBody);
                        Log.e(LOG_TAG,"changeStatus "+ type + " "+response);
                        bStatus.setText("OCUPADO");
                        bStatus.setTextColor(Color.BLACK);
                        bStatus.setBackgroundColor(Color.parseColor("#ffc107"));
                        bStatus.setVisibility(View.VISIBLE);

                        estado.setEnabled(false);
                        estado.setVisibility(View.GONE);

                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putInt(getString(R.string.current_ocupado),1);
                        editor.commit();


                        progressDialog.dismiss();
                        Log.e("dismiss in", "type 1");
                        Toast.makeText(MainActivity.this,"Status cambiado correctamente",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(LOG_TAG,"changeStatus "+ type + " statusCode"+statusCode);
                        progressDialog.dismiss();
                        Log.e("dismiss in", "type 1 fail");
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                break;
            case 2:
                //Llegada a origen
                if (currentService!=0) {
                    url = getString(R.string.mainURL) + "/Servicios/taxi_llega_origen";
                    parametros.put("idServicio", currentService);
                    cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);
                            Log.e(LOG_TAG, "changeStatus " + type + " " + response);
                            llego.setVisibility(View.GONE);
                            llego.setEnabled(false);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt(getString(R.string.current_llego_origen), 1);
                            editor.commit();
                            servicioEnCurso();
                            progressDialog.dismiss();
                            Log.e("dismiss in", "type 2");
                            Toast.makeText(MainActivity.this, "Aviso enviado correctamente", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e(LOG_TAG, "changeStatus " + type + " statusCode" + statusCode);
                            progressDialog.dismiss();
                            Log.e("dismiss in", "type 2 fail");
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión " + statusCode + ".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                }else {
                    Log.e(LOG_TAG, "changeStatus " + type + " currentService=0");
                    progressDialog.dismiss();
                    Log.e("dismiss in", "serviceError 0");
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Error 404.\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                break;
            case 3:
                //Subida de pasajero
                url=getString(R.string.mainURL)+"/Servicios/sube_pasajero";
                parametros.put("idServicio",currentService);

                if (currentService!=0){
                    cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response=new String(responseBody);
                            Log.e(LOG_TAG,"changeStatus "+ type + " "+response);
                            bStatus.setText("Bajada del pasajero");
                            bStatus.setTextColor(Color.WHITE);
                            bStatus.setBackgroundColor(Color.parseColor("#333333"));
                            bStatus.setVisibility(View.VISIBLE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putInt(getString(R.string.current_status),3);
                            editor.commit();
                            servicioEnCurso();
                            progressDialog.dismiss();
                            Log.e("dismiss in", "type 3");
                            Toast.makeText(MainActivity.this,"Status cambiado correctamente",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e(LOG_TAG,"changeStatus "+ type + " statusCode"+statusCode);
                            progressDialog.dismiss();
                            Log.e("dismiss in", "type 3 fail");
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                }else {
                    Log.e(LOG_TAG, "changeStatus " + type + " currentService=0");
                    progressDialog.dismiss();
                    Log.e("dismiss in", "serviceError 0 2");
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Error 404.\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                break;
            case 4:
                //bajada de pasajero
                url=getString(R.string.mainURL)+"/Servicios/baja_pasajero";
                parametros.put("idServicio",currentService);

                if (currentService!=0) {
                    cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);
                            Log.e(LOG_TAG, "changeStatus " + type + " " + response);
                            //Calificar
                            currentStatus=4;
                            progressDialog.dismiss();
                            Log.e("dismiss in", "type 4");
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putInt(getString(R.string.current_status),4);
                            editor.commit();
                            servicioEnCurso();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e(LOG_TAG, "changeStatus " + type + " statusCode" + statusCode);
                            progressDialog.dismiss();
                            Log.e("dismiss in", "type 4 fail");
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión " + statusCode + ".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                }else {
                    Log.e(LOG_TAG, "changeStatus " + type + " currentService=0");
                    progressDialog.dismiss();
                    Log.e("dismiss in", "serviceError 0 3");
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Error 404.\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            default: Log.e(LOG_TAG,"changeStatus type not in scope");
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        if (intent.hasCategory("FMS")) {
            String message = intent.getStringExtra("whatToDo");
            switch (message) {
                case "test":
                    Toast.makeText(MainActivity.this, "Just Testing. Fine.", Toast.LENGTH_SHORT).show();
                    break;
                case "solicitud_servicio":
                    if (!preferences.contains(getString(R.string.current_service))){
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Nuevo servicio disponible.", Snackbar.LENGTH_LONG)
                                .setAction("Ver", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (idUnidad!=0 && idConductor!=0){
                                            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                                    && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                builder.setMessage("No puedes tomar servicios si no activas los permisos requeridos por Mobility.")
                                                        .setTitle(Html.fromHtml("<font color='#000000'>Permisos de ubicacion requeridos</font>"))
                                                        .setCancelable(false)
                                                        .setNegativeButton("Cerrar",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        dialog.cancel();
                                                                    }
                                                                })
                                                        .setPositiveButton("Activar permisos",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                                                                MY_PERMISSIONS_REQUEST_LOCATION);
                                                                    }
                                                                }).show();
                                            }else {
                                                Intent intentS = new Intent(MainActivity.this, ServiciosDisponibles.class);
                                                startActivity(intentS);
                                            }
                                        }else {

                                            if (mGoogleApiClient!=null){
                                                if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                                                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                                                }
                                            }

                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.remove(getString(R.string.p_id_unidad));
                                            editor.remove(getString(R.string.p_id_conductor));
                                            editor.remove(getString(R.string.p_username));
                                            editor.remove(getString(R.string.p_pass));
                                            editor.remove(getString(R.string.p_nick));
                                            editor.remove("active");
                                            editor.commit();

                                            shouldEnd = true;

                                            Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                            startActivity(intentm);
                                            Toast.makeText(MainActivity.this, "Error de datos de sesion", Toast.LENGTH_SHORT).show();
                                            finish();

                                        }

                                    }
                                }).show();
                    }

                    break;

                case "servicio_descartado":
                    //Maybe later
                    break;

                case "servicio_cancelado":
                    if (currentValid && currentService!=0 && currentStatus!=0){
                        SharedPreferences.Editor editorC=preferences.edit();
                        editorC.putInt(getString(R.string.current_cancelado),1);
                        editorC.putInt(getString(R.string.current_cancelado_v),0);
                        editorC.commit();
                        servicioEnCurso();
                    }
                    break;

                case "alerta_panico":
                        //Inform new alerts
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Un conductor de tu zona a emitido una alerta de panico")
                            .setTitle(Html.fromHtml("<font color='#000000'>Alerta de panico</font>"))
                            .setCancelable(false)
                            .setNegativeButton("Cerrar",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            vioAlerta();
                                        }
                                    })
                            .setPositiveButton("Ver alertas",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            vioAlerta();
                                            Intent intentAl = new Intent(MainActivity.this, EmergenciasActivity.class);
                                            startActivity(intentAl);
                                        }
                                    }).show();

                    break;

                case "request_paypal":
                    SharedPreferences.Editor editorRP=preferences.edit();
                    editorRP.putInt(getString(R.string.current_acreditado_pp),0);
                    editorRP.putInt(getString(R.string.current_costo_pp),0);
                    editorRP.putInt(getString(R.string.current_solicitado_pp),1);
                    editorRP.commit();
                    servicioEnCurso();
                    break;

                case "paypal_acreditado":
                    SharedPreferences.Editor editorPA=preferences.edit();
                    editorPA.putInt(getString(R.string.current_acreditado_pp),1);
                    editorPA.putInt(getString(R.string.current_solicitado_pp),1);
                    editorPA.commit();
                    servicioEnCurso();
                    break;

                case "alerta":
                    if (currentLocation!=null){
                        panicRequest();
                        stopService(serviceBlueIntent);
                    }
                    break;

                case "salir":
                    salir("1");
                    break;

                default:
                    Log.e(LOG_TAG, "onNewIntent:" + "whatToDo=" + message);
            }
        }

        if (intent.hasCategory("ServicioAceptado")) {
            invalidateOptionsMenu();

            Log.e(LOG_TAG, "From 4");
            getCurrentStatus();
        }

        if (intent.hasCategory("newToken")){
            String token=getIntent().getStringExtra("token");
            PostToken postToken=new PostToken();
            postToken.post(token,getApplicationContext());
        }

    }

    private void consultarAlertas(){
        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            Log.e("dismiss in","c stat before");
        }
        progressDialog=null;
        if(MainActivity.this.getParent()!=null){
            progressDialog=new ProgressDialog(MainActivity.this.getParent());
        }else{
            progressDialog=new ProgressDialog(MainActivity.this);
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //if is first time not cancellable
        if (isFirstTime || !currentValid){
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading...");
            if(!MainActivity.this.isFinishing()) {
                progressDialog.show();
                Log.e("show in","get c stat");
            }
        }

        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        String url = getString(R.string.mainURL)+"/falta"; //_CONDUCTOR?
        parametros.put("idConductor",idConductor);

        cliente.post(url, parametros, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG," getCurrentStatus alertas not 0");

                final int[] id = new int[1];
                final String[] imageC = new String[1];
                final String[] imageU = new String[1];
                final String[] modelo = new String[1];
                final String[] marca = new String[1];
                final String[] placas = new String[1];
                final String[] nombre = new String[1];
                final String[] nombre2 = new String[1];
                final String[] nombre3 = new String[1];
                final String[] fecha = new String[1];
                final double[] lat =new double[1];
                final double[] lng =new double[1];

                final JSONArray[] jsonArray = new JSONArray[1];
                try {
                    jsonArray[0]=new JSONArray(new String(responseBody));
                    for (int x=0;x<jsonArray[0].length();x++){
                        Log.e("For", x+ " lenght " + jsonArray[0].length());


                        id[0]=jsonArray[0].getJSONObject(x).getInt("idPanico");
                        imageC[0]=jsonArray[0].getJSONObject(x).getString("aFotografia");
                        imageU[0]=jsonArray[0].getJSONObject(x).getString("aFotografiaUnidad");
                        modelo[0]=jsonArray[0].getJSONObject(x).getString("aModelo");
                        marca[0]=jsonArray[0].getJSONObject(x).getString("aMarca");
                        placas[0]=jsonArray[0].getJSONObject(x).getString("aPlacas");
                        nombre[0]=jsonArray[0].getJSONObject(x).getString("aNombreConductor");
                        nombre2[0]=jsonArray[0].getJSONObject(x).getString("aApellidoPatConductor");
                        nombre3[0]=jsonArray[0].getJSONObject(x).getString("aApellidoMatConductor");
                        fecha[0]=jsonArray[0].getJSONObject(x).getString("fFechaHoraAltaAlerta");
                        lat[0]=jsonArray[0].getJSONObject(x).getDouble("dLatitudAlerta");
                        lng[0]=jsonArray[0].getJSONObject(x).getDouble("dLongitudAlerta");

                        String nom=nombre[0]+" "+nombre2[0]+" "+nombre3[0];

                        final Panico panico =new Panico();

                        panico.setIDalerta(id[0]);
                        panico.setImagenC(imageC[0]);
                        panico.setImagenU(imageU[0]);
                        panico.setModelo(modelo[0]);
                        panico.setMarca(marca[0]);
                        panico.setPlacas(placas[0]);
                        panico.setNombre(nom);
                        panico.setFecha(fecha[0]);
                        panico.setLatitud(lat[0]);
                        panico.setLongitud(lng[0]);

                        if (realm!=null && realm.isClosed()){
                            RealmConfiguration config = new RealmConfiguration
                                    .Builder()
                                    .deleteRealmIfMigrationNeeded()
                                    .build();

                            Realm.setDefaultConfiguration(config);

                            realm=Realm.getDefaultInstance();
                            realm.setAutoRefresh(true);
                        }

                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                // This will create a new object in Realm or throw an exception if the
                                // object already exists (same primary key)
                                // realm.copyToRealm(obj);

                                // This will update an existing object with the same primary key
                                // or create a new one if the primary key doesn't exists
                                realm.copyToRealmOrUpdate(panico);
                            }
                        });


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                numAlertas=jsonArray.length;
                int numAP=preferences.getInt(getString(R.string.current_alertas),0);

                if (numAlertas<numAP){
                    int diff=numAP-numAlertas;

                    if (diff==1){
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Hay una alerta nueva.", Snackbar.LENGTH_LONG)
                                .setAction("Ver ", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intentAl = new Intent(MainActivity.this, EmergenciasActivity.class);
                                        startActivity(intentAl);
                                    }
                                }).show();
                    }else {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Hay "+diff+" alertas nuevas.", Snackbar.LENGTH_LONG)
                                .setAction("Ver ", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intentAl = new Intent(MainActivity.this, EmergenciasActivity.class);
                                        startActivity(intentAl);
                                    }
                                }).show();
                    }
                    //Inform new alerts
                }

                SharedPreferences.Editor editor=preferences.edit();
                editor.putInt(getString(R.string.current_alertas),numAlertas);
                editor.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"cA statusCode"+statusCode);
                progressDialog.dismiss();
                Log.e("dismiss in", "cA  fail");
                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void errorUbicaciones(){

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Parece que tenemos problemas la acceder a tu ubicacion, asegurate de aceptar todos los permisos requeridos")
                .setTitle(Html.fromHtml("<font color='#000000'>Advertencia</font>"))
                .setCancelable(false)
                .setNegativeButton("Salir",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                salir("1");
                            }
                        })
                .setPositiveButton("Volver a intentar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (mGoogleApiClient!=null){
                                    if (mGoogleApiClient.isConnected()){
                                        mGoogleApiClient.connect();
                                        Log.e("error location", "API Connecting 3");
                                    }
                                }
                            }
                        }).show();
    }

    private void salir(@Nullable String type) {
        //If puede salir:
        if (type != null){
            if (type.contains("1")) {
                if (mGoogleApiClient!=null){
                    if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                    }
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(getString(R.string.p_id_unidad));
                editor.remove(getString(R.string.p_id_conductor));
                editor.remove(getString(R.string.p_username));
                editor.remove(getString(R.string.p_pass));
                editor.remove(getString(R.string.p_nick));
                editor.remove("active");
                editor.commit();

                shouldEnd = true;

                stopService(serviceBlueIntent);

                Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intentm);
                Toast.makeText(MainActivity.this, "Hasta pronto", Toast.LENGTH_SHORT).show();
                finish();
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("¿Desea cerrar la sesión actual?")
                        .setTitle(Html.fromHtml("<font color='#000000'>Advertencia</font>"))
                        .setCancelable(false)
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Si",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (mGoogleApiClient!=null){
                                            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                                                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                                            }
                                        }
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.remove(getString(R.string.p_id_unidad));
                                        editor.remove(getString(R.string.p_id_conductor));
                                        editor.remove(getString(R.string.p_username));
                                        editor.remove(getString(R.string.p_pass));
                                        editor.remove(getString(R.string.p_nick));
                                        editor.remove("active");
                                        editor.commit();

                                        shouldEnd = true;
                                        stopService(serviceBlueIntent);

                                        dialog.dismiss();
                                        Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intentm);
                                        Toast.makeText(MainActivity.this, "Hasta pronto", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }).show();
            }
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Desea cerrar la sesión actual?")
                    .setTitle(Html.fromHtml("<font color='#000000'>Advertencia</font>"))
                    .setCancelable(false)
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton("Si",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (mGoogleApiClient!=null){
                                        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                                        }
                                    }
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.remove(getString(R.string.p_id_unidad));
                                    editor.remove(getString(R.string.p_id_conductor));
                                    editor.remove(getString(R.string.p_username));
                                    editor.remove(getString(R.string.p_pass));
                                    editor.remove(getString(R.string.p_nick));
                                    editor.remove("active");
                                    editor.commit();

                                    shouldEnd = true;
                                    stopService(serviceBlueIntent);

                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Hasta pronto", Toast.LENGTH_SHORT).show();
                                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intentm);

                                    finish();
                                }
                            }).show();
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
        if (preferences.contains(getString(R.string.current_service)) && preferences.contains(getString(R.string.current_status))) {
            //TODO:
            //Si se encuentra en serivcio modificar menu y acciones
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_locate) {
            if (mMap != null) {
                if (currentLatLng != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                }
            }
            return true;
        }

        if (id == R.id.action_refresh) {
            return true;
        }
        if (id == R.id.action_services) {
            if (!preferences.contains(getString(R.string.current_service))) {
                if (idUnidad!=0 && idConductor!=0){
                    currentOcupado=preferences.getInt(getString(R.string.current_ocupado),0);
                    if (currentOcupado==0) {
                        Intent intentS = new Intent(MainActivity.this, ServiciosDisponibles.class);
                        startActivity(intentS);
                    }else {
                        Toast.makeText(MainActivity.this, "Te encuentras en status ocupado", Toast.LENGTH_LONG).show();
                    }

                }else {
                    if (mGoogleApiClient!=null){
                        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                        }
                    }

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(getString(R.string.p_id_unidad));
                    editor.remove(getString(R.string.p_id_conductor));
                    editor.remove(getString(R.string.p_username));
                    editor.remove(getString(R.string.p_pass));
                    editor.remove(getString(R.string.p_nick));
                    editor.remove("active");
                    editor.commit();

                    shouldEnd = true;

                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intentm);
                    Toast.makeText(MainActivity.this, "Error de datos de sesion", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }else {
                //TODO:
                //Mostrar informacion del servicio en lugar de la actividad de servicios desponibles
                //crear vista

                if (currentRealmService != null) {

                    promptDSS = inflater.inflate(R.layout.datos_servicio, null);
                    dsidserv = (TextView) promptDSS.findViewById(R.id.IDserv);
                    dshorasol = (TextView) promptDSS.findViewById(R.id.HoraSolic);
                    dsnameuser = (TextView) promptDSS.findViewById(R.id.NombreUser);
                    dstel = (TextView) promptDSS.findViewById(R.id.Tel);
                    dsmail = (TextView) promptDSS.findViewById(R.id.eMail);
                    dsorig = (TextView) promptDSS.findViewById(R.id.Orig);
                    dsdest = (TextView) promptDSS.findViewById(R.id.Dest);
                    dsrefer = (TextView) promptDSS.findViewById(R.id.Refer);
                    dstel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:+" + dstel.getText().toString().trim()));
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                // Should we show an explanation?
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        android.Manifest.permission.CALL_PHONE)) {

                                    Snackbar.make(v, "Mobility requiere permisos para realizar llamadas.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
                                            .setAction("Configuración", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    final Intent i = new Intent();
                                                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                                    i.setData(Uri.parse("package:" + getPackageName()));
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                    startActivity(i);
                                                }
                                            }).show();

                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{android.Manifest.permission.CALL_PHONE},
                                            MY_PERMISSIONS_REQUEST_CALL);

                                } else {

                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{android.Manifest.permission.CALL_PHONE},
                                            MY_PERMISSIONS_REQUEST_CALL);

                                }


                            } else {
                                startActivity(callIntent);
                            }

                        }
                    });

                    try{

                    dsidserv.setText(String.valueOf(currentRealmService.getId()));
                    dshorasol.setText(currentRealmService.getFecha());
                    dsnameuser.setText(currentRealmService.getNombre());
                    dstel.setText(String.valueOf(currentRealmService.getTel()));
                    dsmail.setText(currentRealmService.getEmail());
                    dsorig.setText(currentRealmService.getDir_orig());
                    dsdest.setText(currentRealmService.getDir_dest());
                    dsrefer.setText(currentRealmService.getReferencia());

                    if (dialogServicio != null) {
                        dialogServicio = null;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    //Se cierra la vista y el servicio sigue en lista hasta que llegue una push
                                    //para descartarlo
                                }
                            });

                    if (promptDSS.getParent() == null) {
                        dialogServicio = builder.create();
                        dialogServicio.setView(promptDSS);
                        dialogServicio.show();
                    } else {
                        promptDSS = null; //set it to null
                        // now initialized yourView and its component again
                        promptDSS = inflater.inflate(R.layout.datos_servicio, null);
                        dsidserv = (TextView) promptDSS.findViewById(R.id.IDserv);
                        dshorasol = (TextView) promptDSS.findViewById(R.id.HoraSolic);
                        dsnameuser = (TextView) promptDSS.findViewById(R.id.NombreUser);
                        dstel = (TextView) promptDSS.findViewById(R.id.Tel);
                        dsmail = (TextView) promptDSS.findViewById(R.id.eMail);
                        dsorig = (TextView) promptDSS.findViewById(R.id.Orig);
                        dsdest = (TextView) promptDSS.findViewById(R.id.Dest);
                        dsrefer = (TextView) promptDSS.findViewById(R.id.Refer);
                        dstel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:+" + dstel.getText().toString().trim()));
                                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                    // Should we show an explanation?
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                            android.Manifest.permission.CALL_PHONE)) {

                                        Snackbar.make(v, "Mobility requiere permisos para realizar llamadas.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
                                                .setAction("Configuración", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        final Intent i = new Intent();
                                                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                        i.addCategory(Intent.CATEGORY_DEFAULT);
                                                        i.setData(Uri.parse("package:" + getPackageName()));
                                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                        startActivity(i);
                                                    }
                                                }).show();

                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{android.Manifest.permission.CALL_PHONE},
                                                MY_PERMISSIONS_REQUEST_CALL);

                                    } else {

                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{android.Manifest.permission.CALL_PHONE},
                                                MY_PERMISSIONS_REQUEST_CALL);

                                    }


                                } else {
                                    startActivity(callIntent);
                                }

                            }
                        });

                        dsidserv.setText(String.valueOf(currentRealmService.getId()));
                        dshorasol.setText(currentRealmService.getFecha());
                        dsnameuser.setText(currentRealmService.getNombre());
                        dstel.setText(String.valueOf(currentRealmService.getTel()));
                        dsmail.setText(currentRealmService.getEmail());
                        dsorig.setText(currentRealmService.getDir_orig());
                        dsdest.setText(currentRealmService.getDir_dest());
                        dsrefer.setText(currentRealmService.getReferencia());

                        dialogServicio = builder.create();
                        dialogServicio.setView(promptDSS);
                        dialogServicio.show();
                    }

                }catch (Exception ignored){

                }

                    //
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent action;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_balance) {
            //TODO:
            //Mostrar información de pagos:
            //Historial, saldo, opcion para recargar.
            if (idUnidad!=0 && idConductor!=0) {
                action = new Intent(MainActivity.this, Balance.class);
                startActivity(action);
            }
        } else if (id == R.id.nav_con_blue) {
            stopService(serviceBlueIntent);
            startService(serviceBlueIntent);
        }
        else if (id == R.id.nav_historial) {
            action=new Intent(MainActivity.this, Historial.class);
            startActivity(action);
        }else if (id == R.id.nav_eliminar) {

        } else if (id == R.id.nav_alertas) {
            action=new Intent(MainActivity.this, EmergenciasActivity.class);
            startActivity(action);

        } else if (id == R.id.nav_contraseña) {

        } else if (id == R.id.nav_salir) {
            salir(null);

        } else if (id == R.id.nav_servicios) {
            //Actividad de servicios
            if (!preferences.contains(getString(R.string.current_service))) {
                if (idUnidad!=0 && idConductor!=0){
                    currentOcupado=preferences.getInt(getString(R.string.current_ocupado),0);
                    if (currentOcupado==0) {
                        Intent intentS = new Intent(MainActivity.this, ServiciosDisponibles.class);
                        startActivity(intentS);
                    }else {
                        Toast.makeText(MainActivity.this, "Te encuentras en status ocupado", Toast.LENGTH_LONG).show();
                    }

                }else {
                    if (mGoogleApiClient!=null){
                        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                        }
                    }

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(getString(R.string.p_id_unidad));
                    editor.remove(getString(R.string.p_id_conductor));
                    editor.remove(getString(R.string.p_username));
                    editor.remove(getString(R.string.p_pass));
                    editor.remove(getString(R.string.p_nick));
                    editor.remove("active");
                    editor.commit();

                    shouldEnd = true;

                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intentm);
                    Toast.makeText(MainActivity.this, "Error de datos de sesion", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }else {
                //TODO:
                //Mostrar informacion del servicio en lugar de la actividad de servicios desponibles
                //crear vista

                promptDSS = inflater.inflate(R.layout.datos_servicio, null);
                dsidserv = (TextView) promptDSS.findViewById(R.id.IDserv);
                dshorasol = (TextView) promptDSS.findViewById(R.id.HoraSolic);
                dsnameuser = (TextView) promptDSS.findViewById(R.id.NombreUser);
                dstel = (TextView) promptDSS.findViewById(R.id.Tel);
                dsmail = (TextView) promptDSS.findViewById(R.id.eMail);
                dsorig = (TextView) promptDSS.findViewById(R.id.Orig);
                dsdest = (TextView) promptDSS.findViewById(R.id.Dest);
                dsrefer = (TextView) promptDSS.findViewById(R.id.Refer);
                dstel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:+" + dstel.getText().toString().trim()));
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                    android.Manifest.permission.CALL_PHONE)) {

                                Snackbar.make(v, "Mobility requiere permisos para realizar llamadas.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
                                        .setAction("Configuración", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                final Intent i = new Intent();
                                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                                i.setData(Uri.parse("package:" + getPackageName()));
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                startActivity(i);
                                            }
                                        }).show();

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.CALL_PHONE},
                                        MY_PERMISSIONS_REQUEST_CALL);

                            } else {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.CALL_PHONE},
                                        MY_PERMISSIONS_REQUEST_CALL);

                            }


                        }else {
                            startActivity(callIntent);
                        }

                    }
                });

                try{

                dsidserv.setText(String.valueOf(currentRealmService.getId()));
                dshorasol.setText(currentRealmService.getFecha());
                dsnameuser.setText(currentRealmService.getNombre());
                dstel.setText(String.valueOf(currentRealmService.getTel()));
                dsmail.setText(currentRealmService.getEmail());
                dsorig.setText(currentRealmService.getDir_orig());
                dsdest.setText(currentRealmService.getDir_dest());
                dsrefer.setText(currentRealmService.getReferencia());

                if (dialogServicio!=null){
                    dialogServicio=null;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setNeutralButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                //Se cierra la vista y el servicio sigue en lista hasta que llegue una push
                                //para descartarlo
                            }
                        });

                if (promptDSS.getParent() == null) {
                    dialogServicio=builder.create();
                    dialogServicio.setView(promptDSS);
                    dialogServicio.show();
                } else {
                    promptDSS= null; //set it to null
                    // now initialized yourView and its component again
                    promptDSS = inflater.inflate(R.layout.datos_servicio, null);
                    dsidserv = (TextView) promptDSS.findViewById(R.id.IDserv);
                    dshorasol = (TextView) promptDSS.findViewById(R.id.HoraSolic);
                    dsnameuser = (TextView) promptDSS.findViewById(R.id.NombreUser);
                    dstel = (TextView) promptDSS.findViewById(R.id.Tel);
                    dsmail = (TextView) promptDSS.findViewById(R.id.eMail);
                    dsorig = (TextView) promptDSS.findViewById(R.id.Orig);
                    dsdest = (TextView) promptDSS.findViewById(R.id.Dest);
                    dsrefer = (TextView) promptDSS.findViewById(R.id.Refer);
                    dstel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:+" + dstel.getText().toString().trim()));
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                // Should we show an explanation?
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        android.Manifest.permission.CALL_PHONE)) {

                                    Snackbar.make(v, "Mobility requiere permisos para realizar llamadas.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
                                            .setAction("Configuración", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    final Intent i = new Intent();
                                                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                                    i.setData(Uri.parse("package:" + getPackageName()));
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                    startActivity(i);
                                                }
                                            }).show();

                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{android.Manifest.permission.CALL_PHONE},
                                            MY_PERMISSIONS_REQUEST_CALL);

                                } else {

                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{android.Manifest.permission.CALL_PHONE},
                                            MY_PERMISSIONS_REQUEST_CALL);

                                }


                            }else {
                                startActivity(callIntent);
                            }

                        }
                    });

                    dsidserv.setText(String.valueOf(currentRealmService.getId()));
                    dshorasol.setText(currentRealmService.getFecha());
                    dsnameuser.setText(currentRealmService.getNombre());
                    dstel.setText(String.valueOf(currentRealmService.getTel()));
                    dsmail.setText(currentRealmService.getEmail());
                    dsorig.setText(currentRealmService.getDir_orig());
                    dsdest.setText(currentRealmService.getDir_dest());
                    dsrefer.setText(currentRealmService.getReferencia());

                    dialogServicio=builder.create();
                    dialogServicio.setView(promptDSS);
                    dialogServicio.show();
                }


                }catch (Exception ignored){

                }
                //
            }
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
                        Log.e("getPeriodicFusedLocatio", "SUCCES");

                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                            /*
                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                                Snackbar.make(getWindow().getDecorView().getRootView(), "Mobility requiere permisos para acceder a tu ubicación.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
                                        .setAction("Configuración", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                final Intent i = new Intent();
                                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                                i.setData(Uri.parse("package:" + context.getPackageName()));
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                                context.startActivity(i);
                                            }
                                        }).show();

                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);

                                */

                            // } else {

                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);

                            // }

                        }else {
                            if (mGoogleApiClient.isConnected()) {
                                Log.e("error location", "currentLction=" + currentLocation + " api=" + mGoogleApiClient.isConnected() + " firsttime=" + isFirstTime);
                                //mGoogleApiClient.disconnect(); why the fuck?
                                LocationServices.FusedLocationApi.requestLocationUpdates(
                                        mGoogleApiClient, finalMLocationRequest, MainActivity.this);
                            }
                        }


                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            wentSettings=true;
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            Log.e("getPeriodicFusedLocatio", "Error in RESOLUTION_REQUIRED");
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

                        Log.e("getPeriodicFusedLocatio", "Error=SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        Log.e("onActivityResult", "resultCode="+resultCode);
        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        finish();
                        startActivity(getIntent());

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        if (mGoogleApiClient.isConnected()){
                            mGoogleApiClient.disconnect();
                            Log.e("error location", "API Dissconected 2");
                        }
                        wentSettings=true;
                        errorUbicaciones();
                        break;
                    default:
                        break;
                }
                break;
            case 0:
                wentSettings=true;
                errorUbicaciones();
                break;
        }
    }

    private void newLocation() {

        Log.e(LOG_TAG,"newLocation");

        if (mMap!=null){
            if (currentService != 0 && currentStatus!=0 && currentStatus>4){
                if (currentLatLng != null && dest != null) {
                    new DownloadTask().execute(new DirectionsUrl().getUrl(currentLatLng, dest));
                }
            }


        }

    }

    private void rotateMarker(final Marker marker, final float toRotation) {
        final boolean[] isMarkerRotating = {false};
        if(!isMarkerRotating[0]) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 1000;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating[0] = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);

                    float rot = t * toRotation + (1 - t) * startRotation;

                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating[0] = false;
                    }
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;

        stopService(intentService);


        if (isFirstTime) {
            if (!wentSettings){
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Log.e("dismiss in", "OnResume before start");
                }
            progressDialog = null;
            if (MainActivity.this.getParent() != null) {
                progressDialog = new ProgressDialog(MainActivity.this.getParent());
            } else {
                progressDialog = new ProgressDialog(MainActivity.this);
            }
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Comprobando...");
            if (!MainActivity.this.isFinishing()) {
                progressDialog.show();
                Log.e("show in", "onResume");
            }
        }
        }

        if (realm==null){
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);

            realm=Realm.getDefaultInstance();
            realm.setAutoRefresh(true);
        }
        if (realm!=null && realm.isClosed()){
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);

            realm=Realm.getDefaultInstance();
            realm.setAutoRefresh(true);
        }

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        if (!preferences.contains("idUnidad") || !preferences.contains(getString(R.string.p_id_conductor))) {
            if (preferences.contains("active")) {
                dialogiIniciaSesion = null;
                dialogiIniciaSesion = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("¡UPS!")
                        .setMessage("Por favor inicia sesión") //pasar a strings
                        .setCancelable(false)
                        //Agregar codigo de error
                        .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shouldEnd = true;

                                dialog.dismiss();
                                Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intentm);
                                finish();
                            }
                        })
                        .show();
            } else {
                Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intentm);
                finish();
            }

        } else {

            idUnidad = preferences.getInt(getString(R.string.p_id_unidad), 0);
            idConductor = preferences.getInt((getString(R.string.p_id_conductor)), 0);

            if (idUnidad == 0 || idConductor == 0) {
                if (preferences.contains("active")) {
                    dialogiIniciaSesion = null;
                    dialogiIniciaSesion = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("¡UPS!")
                            .setMessage("Por favor inicia sesión") //pasar a strings
                            .setCancelable(false)
                            //Agregar codigo de error
                            .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    shouldEnd = true;

                                    dialog.dismiss();
                                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intentm);
                                    finish();
                                }
                            })
                            .show();
                } else {
                    Intent intentm = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intentm);
                    finish();
                }
            }else {

                if (!wentSettings) {
                    Log.e(LOG_TAG, "From 3");
                    getCurrentStatus();
                }
            }

            naviW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (dest!=null) {
                        double lat = dest.latitude;
                        double lon = dest.longitude;

                        try
                        {
                            String url = "waze://?ll="+lat+","+lon+"&z=10";
                            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                            startActivity( intent );
                        }
                        catch ( ActivityNotFoundException ex  )
                        {
                            Intent intent =
                                    new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
                            startActivity(intent);
                        }
                    }

                }
            });

            naviTW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dest!=null) {
                        double lat = dest.latitude;
                        double lon = dest.longitude;

                        try
                        {
                            String url = "waze://?ll="+lat+","+lon+"&z=10";
                            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                            startActivity( intent );
                        }
                        catch ( ActivityNotFoundException ex  )
                        {
                            Intent intent =
                                    new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
                            startActivity(intent);
                        }
                    }
                }
            });

            naviG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (dest!=null) {
                        double lat = dest.latitude;
                        double lon = dest.longitude;
                        try {
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(MainActivity.this,"Google map error. Intenta actualizar la aplicación",Toast.LENGTH_SHORT).show();
                        }
                    }


                }
            });

            naviTG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (dest!=null) {
                        double lat = dest.latitude;
                        double lon = dest.longitude;
                        try {
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + "");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(MainActivity.this,"Google map error. Intenta actualizar la aplicación",Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });

        }

        if (!delay){
            Log.e("delay false", "!delay1");
            if (mGoogleApiClient != null) {
                if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                    Log.e("error location", "API Connecting 1");
                }
            }
        }

        Log.e(LOG_TAG, "onResume");
        if (mMap != null && currentLatLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
        Log.e(LOG_TAG, "onPause ");

        if (realm!=null && !realm.isClosed()) {
            Log.e(LOG_TAG, "Close realm ");
            realm.close();
        }

        if (!delay){
            if (mGoogleApiClient!=null){
                if (mGoogleApiClient.isConnected()){
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                    mGoogleApiClient.disconnect();
                    Log.e("error location", "API Dissconected 3");
                }
            }
        }

        if (shouldEnd) {
            //Validar no volver a iniciar sesion al regresar
            //validar dejar de mandar ubicaciones
            stopService(intentService);
            stopService(serviceBlueIntent);
        }else {
            //Validar el envio de ubicaciones en segundo plano como service
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//falta avisar no hay permisos
            }else {
                if (!wentSettings){
                    startService(intentService);
                }
            }
        }


    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(polyline!=null){
                polyline.remove();
            }
        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = DownloadAPIDirectionsInfo.downloadUrl(url[0]);
            } catch (Exception e) {
                Log.e("Polylilinea sugerida", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
                Log.e("dismiss in", "postDownloadTask");
            }

            JSONObject jObject;
            try {
                jObject = new JSONObject(result);

                DirectionsJSONParser parser = new DirectionsJSONParser();
                parser.parse(jObject);
                timeAprox = parser.getAproxTime();
                String dist = parser.getDistance();
                String bPoint="destino";
                PolylineOptions polylineOptions = parser.getPolylineOptions();
                polylineOptions.color(0x60000000);
                estado.setVisibility(View.VISIBLE);
                if (currentStatus==2){
                    bPoint="origen";
                }
                if (currentStatus==3){
                    bPoint="destino";
                }
                estado.setText(dist+", "+timeAprox +" aprox a "+bPoint);
                polyline = mMap.addPolyline(polylineOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /*
            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                Snackbar.make(getWindow().getDecorView().getRootView(), "Mobility requiere permisos para acceder a tu ubicación.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
                        .setAction("Configuración", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent i = new Intent();
                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                MainActivity.this.startActivity(i);
                            }
                        }).show();

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                */

            //} else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            //}

        }else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        if (currentLatLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker.getTag()!=null){

                    int id = (int) marker.getTag();

                    if (realm != null) {
                        if (realm.isClosed() || realm.isEmpty()) {
                            realm = null;
                            RealmConfiguration config = new RealmConfiguration
                                    .Builder()
                                    .deleteRealmIfMigrationNeeded()
                                    .build();

                            Realm.setDefaultConfiguration(config);

                            realm = Realm.getDefaultInstance();
                            realm.setAutoRefresh(true);
                        }
                    } else {
                        RealmConfiguration config = new RealmConfiguration
                                .Builder()
                                .deleteRealmIfMigrationNeeded()
                                .build();

                        Realm.setDefaultConfiguration(config);

                        realm = Realm.getDefaultInstance();
                        realm.setAutoRefresh(true);
                    }

                        //Lugar
                        final Lugar tempoL= realm.where(Lugar.class)
                                .equalTo("id", id)
                                .findFirst();
                        Log.e(LOG_TAG,"lugar "+tempoL.toString());
                        Log.e(LOG_TAG,"lugar "+tempoL.toString());

                        final Lugar lugar=new Lugar();
                        lugar.setId(tempoL.getId());
                        lugar.setNombre(tempoL.getNombre());
                        lugar.setDescripcion(tempoL.getDescripcion());
                        if (tempoL.getImagen()!=null) {
                            lugar.setImagen(tempoL.getImagen());
                        }
                        //lugar.setPromo(promo);

                        lugar.setHorario(tempoL.getHorario());
                        lugar.setDireccion(tempoL.getDireccion());
                        lugar.setCategoria(tempoL.getCategoria());
                        lugar.setSitio(tempoL.getSitio());
                        lugar.setCorreo(tempoL.getCorreo());
                        lugar.setTelefono(tempoL.getTelefono());
                        if (tempoL.getIdRuta()!=0) {
                            lugar.setIdRuta(tempoL.getIdRuta());
                            lugar.setOrden(tempoL.getOrden());
                        }

                        final double lat=tempoL.getLatitud();
                        final double lng=tempoL.getLongitud();


                        if (vLugar!=null){
                            vLugar=null;
                        }

                        vLugar=inflater.inflate(R.layout.ventana_lugar,null);
                        vlTitulo=(TextView)vLugar.findViewById(R.id.vLugarTitulo);
                        vlDescripcion=(TextView)vLugar.findViewById(R.id.vLugarDeescripcion);
                        vlDireccion=(TextView)vLugar.findViewById(R.id.vLugarDireccion);
                        vlHorario=(TextView)vLugar.findViewById(R.id.vLugarHorario);
                        vlImagen=(ImageView)vLugar.findViewById(R.id.vLugarImagen);
                        vlTitulo.setText(tempoL.getNombre());
                        vlDescripcion.setText(tempoL.getDescripcionCorta());
                        vlDireccion.setText(tempoL.getDireccion());
                        vlHorario.setText(tempoL.getHorario());

                        if (tempoL.getImagen()!=null && tempoL.getImagen().trim().length()>1) {
                            Picasso.with(MainActivity.this).load(getString(R.string.imagenesLugaresURL) + tempoL.getImagen()).into(vlImagen);
                        }

                        if (dialogLugar!=null){
                            if (dialogLugar.isShowing()){
                                dialogLugar.dismiss();
                            }
                            dialogLugar=null;
                        }

                        AlertDialog.Builder builderL = new AlertDialog.Builder(MainActivity.this)
                                .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                ;

                        if (vLugar.getParent()==null){
                            dialogLugar=builderL.create();
                            dialogLugar.setView(vLugar);
                            dialogLugar.show();
                        }else {
                            vLugar=null;

                            vLugar=inflater.inflate(R.layout.ventana_lugar,null);
                            vlTitulo=(TextView)vLugar.findViewById(R.id.vLugarTitulo);
                            vlDescripcion=(TextView)vLugar.findViewById(R.id.vLugarDeescripcion);
                            vlDireccion=(TextView)vLugar.findViewById(R.id.vLugarDireccion);
                            vlHorario=(TextView)vLugar.findViewById(R.id.vLugarHorario);
                            vlImagen=(ImageView)vLugar.findViewById(R.id.vLugarImagen);

                            vlTitulo.setText(tempoL.getNombre());
                            vlDescripcion.setText(tempoL.getDescripcion());
                            vlDireccion.setText(tempoL.getDireccion());
                            vlHorario.setText(tempoL.getHorario());
                            if (tempoL.getImagen()!=null) {
                                Picasso.with(MainActivity.this).load(tempoL.getImagen()).into(vlImagen);
                            }
                            dialogLugar=builderL.create();
                            dialogLugar.setView(vLugar);
                            dialogLugar.show();
                        }

                        realm.close();
                        //Ends lugar



                }

                return false;
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(LOG_TAG, "onConnected");
        delay=true;
        wentSettings=false;
        if (resumed) {
            getPeriodicFusedLocation();
            Log.e(LOG_TAG, "resumed");
        }else {
            Log.e(LOG_TAG, "not resumed");
            if (mGoogleApiClient!=null){
                if (mGoogleApiClient.isConnected()){
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient!=null){
            if (mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MainActivity.this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(LOG_TAG,"onLocationChanged");
        delay=false;
        if (currentLocation!=null) {
            if (isBetter.isBetterLocation(location,currentBestLocation)) {
                currentBestLocation = location;
                if (idUnidad != 0 && idConductor != 0) {
                    if(resumed) {
                        if (currentService != 0) {
                            Log.e(LOG_TAG, "From 2");
                            getCurrentStatus();
                        }
                        if (currentStatus == 2) {
                            getMyUser();
                        }
                        PostLocation postLocation = new PostLocation();
                        postLocation.post(currentLocation, idUnidad, MainActivity.this);
                        Log.e(LOG_TAG, "onLocationChanged: posting new better location");
                    }
                } else {
                    Log.e(LOG_TAG, "onLocationChanged: idUnidad 0");
                }

            }

            long dif=location.getTime()-currentLocation.getTime();
            long difSecs= TimeUnit.MILLISECONDS.toSeconds(dif);
            Log.e(LOG_TAG, "onLocationChanged: dif="+dif+" secs="+difSecs);

            //Esto para que solo se compruebe cada 5 segundos o mas
            //Por que este metodo puede ser llamado muchas veces en un mismo segundo
            if (difSecs>=5) {
                Log.e(LOG_TAG, "onLocationChanged: 5 or more");

                //Sin importar si la nueva locacion es mejor o no
                // se actualiza la variable sin postear nada y se actualiza la interfaz en newLocation()
                currentLocation = location;
                lasLatLng = currentLatLng;
                currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (resumed && isFirstTime) {
                    newLocation();
                    if (mMap!=null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        Log.e("dismiss in", "oLC fisrttime resumed");
                    }
                    isFirstTime = false;
                }
                if (resumed && !currentValid) {
                    Log.e(LOG_TAG, "From 1");
                    getCurrentStatus();
                }
            }

        }else {
            //Solo de damos el primer valor a la locacion y derivados y posteamos
            currentLocation=location;
            lasLatLng=currentLatLng;
            currentLatLng=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

            if (idUnidad!=0){
                if (resumed && isFirstTime) {
                    newLocation();
                    if (mMap!=null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        Log.e("dismiss in", "oLC fisrttime resumed");
                    }
                    isFirstTime = false;

                    if (currentService != 0) {
                        Log.e(LOG_TAG, "From 2");
                        getCurrentStatus();
                    }
                    if (currentStatus == 2) {
                        getMyUser();
                    }
                    PostLocation postLocation = new PostLocation();
                    postLocation.post(currentLocation, idUnidad, MainActivity.this);
                    Log.e(LOG_TAG, "onLocationChanged: posting new better location");
                }
            }else {
                Log.e(LOG_TAG, "onLocationChanged: idUnidad 0");
            }
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        wentSettings=true;
        errorUbicaciones();

    }
}
