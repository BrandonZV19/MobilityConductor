package com.softtim.mobilityconductor;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;


import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class Historial extends AppCompatActivity {
    Realm realm;
    ListView listView;
    TextView nothing;
    RealmResults<Servicio> results;
    ProgressDialog progressDialog=null;
    List<Servicio> listaServicios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        nothing=(TextView)findViewById(R.id.nothingHistorial);
        listView = (ListView) findViewById(R.id.listViewHistorial);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.simplebar);
            actionBar.setTitle("Historial");
        }
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

        if (progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        if(getParent()!=null){
            progressDialog=new ProgressDialog(getParent());
        }else{
            progressDialog=new ProgressDialog(Historial.this);
        }

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Cargando...");

        if(!Historial.this.isFinishing())
        {
            if (progressDialog!=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                Log.e("dismiss in","c stat before");
            }
            progressDialog=null;
            if(Historial.this.getParent()!=null){
                progressDialog=new ProgressDialog(Historial.this.getParent());
            }else{
                progressDialog=new ProgressDialog(Historial.this);
            }
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading...");
            if(!Historial.this.isFinishing()) {
                progressDialog.show();
                Log.e("show in","");
            }
        }

        //Historial de otros usuarios? Agregar id de usuarios a los servicios

        realm=Realm.getDefaultInstance();
        realm.setAutoRefresh(true);

        results = realm.where(Servicio.class)
                .greaterThanOrEqualTo("status",2)
                .findAllAsync();

        if (results.isLoaded()){
            if (listaServicios!=null){
                listaServicios.clear();
            }
            listaServicios=results;
            if (listaServicios.size()>0) {
                listView.setAdapter(new HistorialAdapter(Historial.this, R.layout.row_historial, listaServicios));
                nothing.setVisibility(View.GONE);
            }else {
                nothing.setVisibility(View.VISIBLE);
            }
        }

        progressDialog.dismiss();

        results.addChangeListener(new RealmChangeListener<RealmResults<Servicio>>() {
            @Override
            public void onChange(RealmResults<Servicio> element) {
                Log.e("HistorialAct","onChange");
                new Historial.getServices().execute();
            }
        });


    }

    private class getServices extends AsyncTask<Object, Object, List<Servicio>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // init progressdialog

            if (progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            if(getParent()!=null){
                progressDialog=new ProgressDialog(getParent());
            }else{
                progressDialog=new ProgressDialog(Historial.this);
            }

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Cargando...");

            if(!Historial.this.isFinishing())
            {
                if (progressDialog!=null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Log.e("dismiss in","c stat before");
                }
                progressDialog=null;
                if(Historial.this.getParent()!=null){
                    progressDialog=new ProgressDialog(Historial.this.getParent());
                }else{
                    progressDialog=new ProgressDialog(Historial.this);
                }
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Loading...");
                if(!Historial.this.isFinishing()) {
                    progressDialog.show();
                    Log.e("show in","");
                }
            }
            listView.setAdapter(null);
        }



        @Override
        protected List<Servicio> doInBackground(Object... params) {
            // get data
            // Get a Realm instance for this thread

            // Or alternatively do the same all at once (the "Fluent interface"):
            Realm realm2=Realm.getDefaultInstance();
            RealmResults tempo = realm2.where(Servicio.class)
                    .greaterThanOrEqualTo("status",2)
                    .findAll();

            Log.e("HistorialAct bckgrnd",tempo.toString());
            List<Servicio> listTemp= realm2.copyFromRealm(tempo);
            realm2.close();

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
            if (listaServicios.size()>0) {
                listView.setAdapter(new HistorialAdapter(Historial.this, R.layout.row_historial, listaServicios));
                nothing.setVisibility(View.GONE);
            }else {
                nothing.setVisibility(View.VISIBLE);
            }

            if (progressDialog!=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
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
