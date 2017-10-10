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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;

public class EmergenciasActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Panico> alertArrayList;
    ProgressDialog progressDialog;
    String TAG="EmergenciasAct";
    TextView nothing;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergencias);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.simplebar);
            actionBar.setTitle("Alertas de panico");
        }

        listView=(ListView)findViewById(R.id.listEmergencias);
        nothing=(TextView)findViewById(R.id.tvNoPanic);

        alertArrayList=new ArrayList<>();

        //consultarAlertas();
        new getAlertas().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    //Conductor/alertas_conductor
    //recibe id conductor
    public void consultarAlertas(){
        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog=null;

        if(EmergenciasActivity.this.getParent()!=null){
            progressDialog=new ProgressDialog(EmergenciasActivity.this.getParent());
        }else{
            progressDialog=new ProgressDialog(EmergenciasActivity.this);
        }

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("...");

        if(!EmergenciasActivity.this.isFinishing())
        {
            progressDialog.show();
        }

        final int[] id = new int[1];
        final String[] imageC = new String[1];
        final String[] imageU = new String[1];
        final String[] modelo = new String[1];
        final String[] marca = new String[1];
        final String[] placas = new String[1];
        final String[] nombre = new String[1];
        final String[] fecha = new String[1];
        final double[] lat =new double[1];
        final double[] lng =new double[1];

        AsyncHttpClient client=new AsyncHttpClient();
        String url=getString(R.string.mainURL)+ " ";
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (progressDialog!=null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Log.e("CategoriesSucces",new String(responseBody)+"");

                final JSONArray[] jsonArray = new JSONArray[1];
                try {
                    jsonArray[0]=new JSONArray(new String(responseBody));
                    for (int x=0;x<jsonArray[0].length();x++){

                        Log.e("For", x+ " lenght " + jsonArray[0].length());

                        id[0]=jsonArray[0].getJSONObject(x).getInt(" ");
                        imageC[0]=jsonArray[0].getJSONObject(x).getString(" ");
                        imageU[0]=jsonArray[0].getJSONObject(x).getString(" ");
                        modelo[0]=jsonArray[0].getJSONObject(x).getString(" ");
                        marca[0]=jsonArray[0].getJSONObject(x).getString(" ");
                        placas[0]=jsonArray[0].getJSONObject(x).getString(" ");
                        nombre[0]=jsonArray[0].getJSONObject(x).getString(" ");
                        fecha[0]=jsonArray[0].getJSONObject(x).getString(" ");
                        lat[0]=jsonArray[0].getJSONObject(x).getDouble(" ");
                        lng[0]=jsonArray[0].getJSONObject(x).getDouble(" ");

                        final Panico panico =new Panico();

                        panico.setImagenC(imageC[0]);
                        panico.setImagenU(imageU[0]);
                        panico.setModelo(modelo[0]);
                        panico.setMarca(marca[0]);
                        panico.setPlacas(placas[0]);
                        panico.setNombre(nombre[0]);
                        panico.setLatitud(lat[0]);
                        panico.setLongitud(lng[0]);
                        panico.setFecha(fecha[0]);

                        alertArrayList.add(panico);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Catch", ""+e.toString() );
                }

                Log.e("Out", ""+alertArrayList.size());
                listView.setAdapter(new AdaptadorEmergencias(EmergenciasActivity.this,alertArrayList));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (progressDialog!=null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private class getAlertas extends AsyncTask<Object, Object, ArrayList<Panico>> {

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
                progressDialog=new ProgressDialog(EmergenciasActivity.this);
            }

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Cargando...");

            if(!EmergenciasActivity.this.isFinishing())
            {
                if (progressDialog!=null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Log.e("dismiss in","c stat before");
                }
                progressDialog=null;
                if(EmergenciasActivity.this.getParent()!=null){
                    progressDialog=new ProgressDialog(EmergenciasActivity.this.getParent());
                }else{
                    progressDialog=new ProgressDialog(EmergenciasActivity.this);
                }
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Loading...");
                if(!EmergenciasActivity.this.isFinishing()) {
                    progressDialog.show();
                    Log.e("show in","");
                }
            }
            listView.setAdapter(null);
        }



        @Override
        protected ArrayList<Panico> doInBackground(Object... params) {
            // get data
            // Get a Realm instance for this thread

            // Or alternatively do the same all at once (the "Fluent interface"):
            Realm realm2=Realm.getDefaultInstance();
            RealmResults tempo = realm2.where(Panico.class)
                    .findAll();

            Log.e(TAG,tempo.toString());
            ArrayList<Panico> listTemp= (ArrayList<Panico>) realm2.copyFromRealm(tempo);
            realm2.close();

            return listTemp;

        }


        @Override
        protected void onPostExecute(ArrayList<Panico> result) {
            super.onPostExecute(result);
            // dismiss dialog

            if (alertArrayList!=null){
                alertArrayList.clear();
            }
            alertArrayList=result;
            if (alertArrayList.size()>0) {
                listView.setAdapter(new AdaptadorEmergencias(EmergenciasActivity.this, alertArrayList));
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
