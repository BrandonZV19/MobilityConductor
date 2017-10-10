package com.softtim.mobilityconductor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class ServiciosDisponibles extends AppCompatActivity {

    ListView listView;
    TextView nothing;
    List<Servicio> listaServicios;
    String TAG="ServiciosDisponibles";
    private ProgressDialog progressDialog = null;
    Realm realm;
    RealmResults<Servicio> results;
    SharedPreferences preferences;
    int idUnidad, idConductor,currentStatus, currentOcupado;
    boolean currentValid=false;
    ArrayList<Servicio> servicesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios_disponibles);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.simplebar);
        toolbar.setTitle("Servicios");
        setSupportActionBar(toolbar);

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        servicesList=new ArrayList<>();

        if (!preferences.contains("idUnidad") || !preferences.contains(getString(R.string.p_id_conductor))) {
            Intent intentm = new Intent(ServiciosDisponibles.this, MainActivity.class);
            startActivity(intentm);
            finish();//Lo manda al main, y el main lo regresara a iniciar sesion
        }else {
            idUnidad = preferences.getInt(getString(R.string.p_id_unidad), 0);
            idConductor = preferences.getInt((getString(R.string.p_id_conductor)),0);
            if (idUnidad==0 || idConductor==0){
                Intent intentm = new Intent(ServiciosDisponibles.this, MainActivity.class);
                startActivity(intentm);
                finish();
                //Lo manda al main, y el main lo regresara a iniciar sesion
            }
        }

        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            Log.e("dismiss in"," gg 2");
        }
        progressDialog=null;

        progressDialog=new ProgressDialog(ServiciosDisponibles.this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        if(!ServiciosDisponibles.this.isFinishing()) {
            progressDialog.show();
            Log.e("show in","gg 2");
        }

        nothing=(TextView)findViewById(R.id.noServices);
        listView = (ListView) findViewById(R.id.sdList);

        //Realm.init(getApplicationContext());
        try {
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);
        }catch (Exception ignored){
            Realm.init(getApplicationContext());
            RealmConfiguration config = new RealmConfiguration
                    .Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);
        }
        realm=Realm.getDefaultInstance();
        realm.setAutoRefresh(true);

        results = realm.where(Servicio.class)
                .equalTo("status",1)
                .findAllAsync();
        if (results.isLoaded()){
            if (listaServicios!=null){
                listaServicios.clear();
            }
            listaServicios=results;
            if (listaServicios.size()>0) {
                listView.setAdapter(new SDAdapter(ServiciosDisponibles.this, R.layout.row_item, listaServicios));
                nothing.setVisibility(View.GONE);
            }else {
                nothing.setVisibility(View.VISIBLE);
            }
        }
        progressDialog.dismiss();
        Log.e("dismiss in"," gg 3");

        results.addChangeListener(new RealmChangeListener<RealmResults<Servicio>>() {
            @Override
            public void onChange(RealmResults<Servicio> element) {
                Log.e(TAG,"onChange");
                new ServiciosDisponibles.getServices().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        // Start a new thread that will download all the data
        // new ServiciosDisponibles.getServices().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //new ServiciosDisponibles.getServices().execute();
        
        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        if (!preferences.contains("idUnidad") || !preferences.contains(getString(R.string.p_id_conductor))) {
            Intent intentm = new Intent(ServiciosDisponibles.this, MainActivity.class);
            startActivity(intentm);
            finish();//Lo manda al main, y el main lo regresara a iniciar sesion
        }else {
            idUnidad = preferences.getInt(getString(R.string.p_id_unidad), 0);
            idConductor = preferences.getInt((getString(R.string.p_id_conductor)),0);
            if (idUnidad!=0 && idConductor!=0){
                //consultar
                currentStatus=preferences.getInt(getString(R.string.current_status),0);
                if (currentStatus>=2){
                    Intent intentm = new Intent(ServiciosDisponibles.this, MainActivity.class);
                    startActivity(intentm);
                    finish();
                }else {
                    currentOcupado=preferences.getInt(getString(R.string.current_ocupado),0);
                    if (currentOcupado==0) {
                        getCurrentServices();
                    }else {
                        Intent intentm = new Intent(ServiciosDisponibles.this, MainActivity.class);
                        startActivity(intentm);
                        finish();
                    }
                }

            }else {
                Intent intentm = new Intent(ServiciosDisponibles.this, MainActivity.class);
                startActivity(intentm);
                finish();//Lo manda al main, y el main lo regresara a iniciar sesion
            }
        }
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (realm!=null && !realm.isClosed()) {
            Log.e(TAG, "Close realm ");
            realm.close();
        }
    }

    private void getCurrentServices(){
        /** TODO:
         * Retorna un json con la siguiente información:
         *
         */
        
        final JSONArray[] serviciosActivos = new JSONArray[1];

        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            Log.e("dismiss in"," gg 4");
        }
        progressDialog=null;
        if(ServiciosDisponibles.this.getParent()!=null){
            progressDialog=new ProgressDialog(ServiciosDisponibles.this.getParent());
        }else{
            progressDialog=new ProgressDialog(ServiciosDisponibles.this);
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        if(!ServiciosDisponibles.this.isFinishing()) {
            progressDialog.show();
            Log.e("show in","gg 3");
        }

        Log.e(TAG," getCurrentServices before "+ idConductor);

        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        String url = getString(R.string.mainURL)+"/Conductor/servicios_conductor";
        parametros.put("idConductor",idConductor);
        //Mandar token prob.
        cliente.post(url, parametros, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(TAG," getCurrentServices response:"+ new String(responseBody));
                currentValid=true;

                if (servicesList!=null){
                    servicesList.clear();
                }
                
                try {
                    serviciosActivos[0] = new JSONArray(new String(responseBody));

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

                        for (int x = 0; x < serviciosActivos[0].length(); x++) {
                            //Get every service data and put it in the list, then update the realm database
                            //with these services, only with these services, the other servies that may exists
                            //in the database are no longer valid

                            int id;
                            String nombre;
                            String email;
                            String fecha;
                            double lat_d;
                            double lng_d;
                            double lat_o;
                            double lng_o;
                            String referencia;
                            int status;
                            String tel;
                            String dir_d;
                            String col_d;
                            String dir_o;
                            String col_o;


                            id=         serviciosActivos[0].getJSONObject(x).getInt("idServicio");
                            nombre=     serviciosActivos[0].getJSONObject(x).getString("aNombreUsuario");

                            email=      serviciosActivos[0].getJSONObject(x).getString("aEmail");
                            fecha=      serviciosActivos[0].getJSONObject(x).getString("fFechaHoraSolicitud");
                            lat_d=      serviciosActivos[0].getJSONObject(x).getDouble("dLatitudDestino");
                            lng_d=      serviciosActivos[0].getJSONObject(x).getDouble("dLongitudDestino");
                            lat_o=      serviciosActivos[0].getJSONObject(x).getDouble("dLatitudOrigen");
                            lng_o=      serviciosActivos[0].getJSONObject(x).getDouble("dLongitudOrigen");
                            referencia= serviciosActivos[0].getJSONObject(x).getString("aReferencia");

                            status=     serviciosActivos[0].getJSONObject(x).getInt("idStatus");
                            tel=        serviciosActivos[0].getJSONObject(x).getString("aTelefono");

                            dir_d=      serviciosActivos[0].getJSONObject(x).getString("aDireccionDestino");
                            col_d=      serviciosActivos[0].getJSONObject(x).getString("aColoniaDestino");
                            dir_o=      serviciosActivos[0].getJSONObject(x).getString("aDireccionOrigen");
                            col_o=      serviciosActivos[0].getJSONObject(x).getString("aColoniaOrigen");

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

                            servicesList.add(servicio);
                            
                        }

                        final RealmResults<Servicio> results = realm.where(Servicio.class).equalTo("status",1).findAll();

                        // All changes to data must happen in a transaction
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                results.deleteAllFromRealm();
                                realm.copyToRealmOrUpdate(servicesList);
                            }
                        });

                        //realm.close();


                    new ServiciosDisponibles.getServices().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);;
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSONException",e.toString());
                }

                if (progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();Log.e("dismiss in"," gg 5");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG,"getCurrentServices statusCode"+statusCode);
                //TODO: Importante
                //Fallo al consutlar el status actual, la funcionalidad de la app esta comprometida.
                //Ejemplo: el conductor podria recibir o aceptar un nuevo servicio que resultaria en errores e incosistencias
                //Comprobar currentValid antes de cada accion
                currentValid=false;
                progressDialog.dismiss();
                Log.e("dismiss in"," gg 6");
                Snackbar.make(getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nIntentando consultar el status actual.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG,"onNewIntent");
        new ServiciosDisponibles.getServices().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class getServices extends AsyncTask<Object, Object, List<Servicio>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // init progressdialog

            Log.e("PRE", "fuck you");

            if (progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
                Log.e("dismiss in"," gg 7");
            }

            if(getParent()!=null){
                progressDialog=new ProgressDialog(getParent());
            }else{
                progressDialog=new ProgressDialog(ServiciosDisponibles.this);
            }

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Cargando...");

            if(!ServiciosDisponibles.this.isFinishing())
            {
                         if (progressDialog!=null && progressDialog.isShowing()) {
                             progressDialog.dismiss();
                             Log.e("dismiss in"," gg 7");
                         }
                         progressDialog=null;
                if(ServiciosDisponibles.this.getParent()!=null){
                    progressDialog=new ProgressDialog(ServiciosDisponibles.this.getParent());
                }else{
                    progressDialog=new ProgressDialog(ServiciosDisponibles.this);
                }
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Loading...");
                if(!ServiciosDisponibles.this.isFinishing()) {
                    progressDialog.show();
                    Log.e("show in"," gg 1");
                }
            }
            listView.setAdapter(null);
        }



        @Override
        protected List<Servicio> doInBackground(Object... params) {
            // get data
            // Get a Realm instance for this thread

            // Or alternatively do the same all at once (the "Fluent interface"):
            Log.e("BackGround", "fuck you");

            List<Servicio> listTemp = null;
            try {
                Realm realm2 = Realm.getDefaultInstance();
                RealmResults tempo = realm2.where(Servicio.class)
                        .equalTo("status", 1)
                        .findAll();

                Log.e("BackGround", tempo.toString());
               
                listTemp = realm2.copyFromRealm(tempo);
                realm2.close();

                return listTemp;
            }catch (Exception e){
                Log.e("BackGround exception",e.toString());
                
            }
            return listTemp;
        }


        @Override
        protected void onPostExecute(List<Servicio> result) {
            super.onPostExecute(result);
            // dismiss dialog

            if (listaServicios!=null){
                listaServicios.clear();
            }
            listaServicios=result;
            Log.e("PostExecute result",listaServicios.toString());
            if (listaServicios.size()>0) {
                listView.setAdapter(new SDAdapter(ServiciosDisponibles.this, R.layout.row_item, listaServicios));
                nothing.setVisibility(View.GONE);
            }else {
                nothing.setVisibility(View.VISIBLE);
            }

            if (progressDialog!=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                Log.e("dismiss in"," gg 1");
            }

        }
    }

}
