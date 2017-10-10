package com.softtim.mobilityconductor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by softtim on 11/28/16.
 */

class SDAdapter extends ArrayAdapter<Servicio> {
    private static final int MY_PERMISSIONS_REQUEST_CALL = 9;
    private int resource,idConductor,idUnidad;
    private LayoutInflater inflater;
    private Context context;
    private String LOG_TAG="SDAdapter";
    private View promptDSS;
    private TextView idserv, horasol, nameuser, tel, mail, orig, dest, refer;
    private AlertDialog AcepRech;
    private SharedPreferences preferences;
    private Realm realm;
    private ProgressDialog progressDialog;


    SDAdapter(Context ctx, int resourceId, List<Servicio> objects) {

        super(ctx, resourceId, objects);
        resource = resourceId;
        inflater = LayoutInflater.from(ctx);
        context = ctx;

        progressDialog=new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Espere. . .");

        preferences=context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);

    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

		/* create a new view of my layout and inflate it in the row */
        convertView = inflater.inflate(resource, null);

        final Servicio serv = getItem(position);
        final String cO, cD, completeO, completeD, fecha;
        final int iddd;

        assert serv != null;
        cO=serv.getColonia_orig();
        cD=serv.getColonia_dest();

        completeO=serv.getDir_orig();
        completeD=serv.getDir_dest();
        fecha=serv.getFecha();

        iddd=serv.getId();

		//Take the TextView from layout and set the colonia de origen
        TextView txtOrigen = (TextView) convertView.findViewById(R.id.sdOrigen);
        TextView txtDest = (TextView) convertView.findViewById(R.id.sdDestino);
        TextView txtId= (TextView)convertView.findViewById(R.id.sdIdServ);
        TextView txtTitCO=(TextView)convertView.findViewById(R.id.cdco);
        TextView txtTitCD=(TextView)convertView.findViewById(R.id.cdcd);
        TextView txtTitId=(TextView)convertView.findViewById(R.id.sdtvid);
        TextView txtFecha=(TextView)convertView.findViewById(R.id.sdFecha);

        if (position%2==0){
             txtFecha.setTextColor(Color.parseColor("#523000"));
            txtOrigen.setTextColor(Color.parseColor("#523000"));
              txtDest.setTextColor(Color.parseColor("#523000"));
                txtId.setTextColor(Color.parseColor("#523000"));
             txtTitCO.setTextColor(Color.parseColor("#523000"));
             txtTitCD.setTextColor(Color.parseColor("#523000"));
             txtTitId.setTextColor(Color.parseColor("#523000"));
        }

        txtFecha.setText(fecha);
        txtOrigen.setText(cO);
        txtDest.setText(cD);
        txtId.setText(String.valueOf(iddd));

        promptDSS = inflater.inflate(R.layout.datos_servicio, null);
        idserv = (TextView) promptDSS.findViewById(R.id.IDserv);
        horasol = (TextView) promptDSS.findViewById(R.id.HoraSolic);
        nameuser = (TextView) promptDSS.findViewById(R.id.NombreUser);
        tel = (TextView) promptDSS.findViewById(R.id.Tel);
        mail = (TextView) promptDSS.findViewById(R.id.eMail);
        orig = (TextView) promptDSS.findViewById(R.id.Orig);
        dest = (TextView) promptDSS.findViewById(R.id.Dest);
        refer = (TextView) promptDSS.findViewById(R.id.Refer);
        tel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:+" + tel.getText().toString().trim()));
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                            android.Manifest.permission.CALL_PHONE)) {

                        Snackbar.make(v, "Mobility requiere permisos para realizar llamadas.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
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
                                new String[]{Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL);

                    } else {

                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL);

                    }


                }else {
                    context.startActivity(callIntent);
                }
            }
        });

            idserv.setText(String.valueOf(serv.getId()));
            horasol.setText(serv.getFecha());
            nameuser.setText(serv.getNombre());
            tel.setText(String.valueOf(serv.getTel()));
            mail.setText(serv.getEmail());
            orig.setText(completeO);
            dest.setText(completeD);
            refer.setText(serv.getReferencia());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AcepRech!=null){
                    AcepRech=null;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setNegativeButton("Rechazar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (progressDialog!=null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                progressDialog=null;

                                if(((Activity)(context)).getParent()!=null){
                                    progressDialog=new ProgressDialog(((Activity)(context)).getParent());
                                }else{
                                    progressDialog=new ProgressDialog(context);
                                }

                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.setTitle("Cargando...");

                                if(!( (Activity) context).isFinishing())
                                {
                                    progressDialog.show();
                                }

                                if (preferences.contains(context.getString(R.string.p_id_conductor))){
                                    idConductor=preferences.getInt(context.getString(R.string.p_id_conductor),0);
                                    if (idConductor!=0){
                                        AsyncHttpClient cliente = new AsyncHttpClient();
                                        RequestParams parametros = new RequestParams();
                                        String url = context.getString(R.string.mainURL)+"/Servicios/descartar_servicio";
                                        parametros.put("idConductor", idConductor);
                                        parametros.put("idServicio", serv.getId());
                                        Log.e(LOG_TAG,"Rechazar panico "+idConductor+" servicio "+serv.getId());
                                        cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                String response= new String(responseBody);
                                                Log.e(LOG_TAG,"Rechazar response"+response);

                                                RealmConfiguration config = new RealmConfiguration
                                                        .Builder()
                                                        .deleteRealmIfMigrationNeeded()
                                                        .build();

                                                Realm.setDefaultConfiguration(config);

                                                realm=Realm.getDefaultInstance();
                                                realm.setAutoRefresh(true);

                                                final int idd=serv.getId();

                                                final RealmResults<Servicio> results = realm.where(Servicio.class).equalTo("id",idd).findAll();

                                                // All changes to data must happen in a transaction
                                                realm.executeTransaction(new Realm.Transaction() {
                                                    @Override
                                                    public void execute(Realm realm) {
                                                        results.deleteAllFromRealm();


                                                    }
                                                });
                                                realm.close();

                                                if (progressDialog!=null && progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }
                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                Log.e(LOG_TAG,"Rechazar statusCode"+statusCode);
                                                if (progressDialog!=null && progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }
                                                Snackbar.make(((Activity)(context)).getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        });
                                    }
                                }



                            }
                        })
                        .setNeutralButton("Regresar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                //Se cierra la vista y el servicio sigue en lista hasta que llegue una push
                                //para descartarlo
                            }
                        })
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO:
                                //A partir de aqui el panico se encuentra en servicio, no puede ver servicios disponibles
                                //Modificar las preferencias

                                if (progressDialog!=null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                progressDialog=null;

                                if(((Activity)(context)).getParent()!=null){
                                    progressDialog=new ProgressDialog(((Activity)(context)).getParent());
                                }else{
                                    progressDialog=new ProgressDialog(context);
                                }

                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.setTitle("Cargando...");

                                if(!( (Activity) context).isFinishing())
                                {
                                    progressDialog.show();
                                }

                                if (preferences.contains(context.getString(R.string.p_id_unidad))){
                                    idUnidad=preferences.getInt(context.getString(R.string.p_id_unidad),0);
                                    if (preferences.contains(context.getString(R.string.p_id_conductor))){
                                        idConductor=preferences.getInt(context.getString(R.string.p_id_conductor),0);
                                        if (idConductor!=0){
                                            if (idUnidad!=0){
                                                AsyncHttpClient cliente = new AsyncHttpClient();
                                                RequestParams parametros = new RequestParams();
                                                String url = context.getString(R.string.mainURL)+"/Servicios/aceptar_servicio";
                                                parametros.put("idConductor", idConductor);
                                                parametros.put("idUnidad", idUnidad);
                                                parametros.put("idServicio", serv.getId());
                                                Log.e(LOG_TAG,"Aceptar panico "+idConductor+" servicio "+serv.getId());
                                                cliente.post(url, parametros, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                        String response= new String(responseBody);
                                                        Log.e(LOG_TAG,"Aceptar response"+response);

                                                        if (response.contains("nvalido")){
                                                            Toast.makeText(context,"Alguien mas ha aceptado este servicio",Toast.LENGTH_LONG).show();
                                                        }else {
                                                            RealmConfiguration config = new RealmConfiguration
                                                                    .Builder()
                                                                    .deleteRealmIfMigrationNeeded()
                                                                    .build();

                                                            Realm.setDefaultConfiguration(config);

                                                            realm=Realm.getDefaultInstance();
                                                            realm.setAutoRefresh(true);

                                                            //final RealmResults<Servicio> results = realm.where(Servicio.class).equalTo("id",serv.getId()).findAll();

                                                            // All changes to data must happen in a transaction
                                                            realm.executeTransaction(new Realm.Transaction() {
                                                                @Override
                                                                public void execute(Realm realm) {
                                                                    Servicio result = realm.where(Servicio.class).equalTo("id",serv.getId()).findFirst();
                                                                    result.setStatus(2);
                                                                    realm.insertOrUpdate(result);
                                                                }
                                                            });

                                                            realm.close();

                                                            SharedPreferences.Editor editor=preferences.edit();
                                                            editor.putInt(context.getString(R.string.current_service),serv.getId());
                                                            editor.putInt(context.getString(R.string.current_status),2);
                                                            editor.putInt(context.getString(R.string.current_confirmado),1);
                                                            editor.putInt(context.getString(R.string.current_cancelado),0);
                                                            editor.putInt(context.getString(R.string.current_cancelado_v),0);
                                                            editor.commit();

                                                            Intent intent = new Intent(context, MainActivity.class)
                                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            intent.addCategory("ServicioAceptado");
                                                            if (progressDialog!=null && progressDialog.isShowing()) {
                                                                progressDialog.dismiss();
                                                            }

                                                            context.startActivity(intent);
                                                            ((Activity)(context)).finish();
                                                        }

                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                        Log.e(LOG_TAG,"Aceptar statusCode"+statusCode);
                                                        if (progressDialog!=null && progressDialog.isShowing()) {
                                                            progressDialog.dismiss();
                                                        }
                                                        Snackbar.make(((Activity)(context)).getWindow().getDecorView().getRootView(), "Error de conexión "+statusCode+".\nPor favor intentalo de nuevo mas tarde.", Snackbar.LENGTH_LONG)
                                                                .setAction("Action", null).show();
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                            }
                        });
                if (promptDSS.getParent() == null) {
                    AcepRech=builder.create();
                    AcepRech.setView(promptDSS);
                    AcepRech.show();
                } else {
                    promptDSS= null; //set it to null
                    // now initialized yourView and its component again
                    promptDSS = inflater.inflate(R.layout.datos_servicio, null);
                    idserv = (TextView) promptDSS.findViewById(R.id.IDserv);
                    horasol = (TextView) promptDSS.findViewById(R.id.HoraSolic);
                    nameuser = (TextView) promptDSS.findViewById(R.id.NombreUser);
                    tel = (TextView) promptDSS.findViewById(R.id.Tel);
                    mail = (TextView) promptDSS.findViewById(R.id.eMail);
                    orig = (TextView) promptDSS.findViewById(R.id.Orig);
                    dest = (TextView) promptDSS.findViewById(R.id.Dest);
                    refer = (TextView) promptDSS.findViewById(R.id.Refer);
                    tel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:+" + tel.getText().toString().trim()));
                            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                // Should we show an explanation?
                                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                                        android.Manifest.permission.CALL_PHONE)) {

                                    Snackbar.make(v, "Mobility requiere permisos para realizar llamadas.\nPor favor habilitalos en la configuración de tu dispositivo.", Snackbar.LENGTH_LONG)
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
                                            new String[]{Manifest.permission.CALL_PHONE},
                                            MY_PERMISSIONS_REQUEST_CALL);

                                } else {

                                    ActivityCompat.requestPermissions((Activity) context,
                                            new String[]{Manifest.permission.CALL_PHONE},
                                            MY_PERMISSIONS_REQUEST_CALL);

                                }


                            }else {
                                context.startActivity(callIntent);
                            }

                        }
                    });

                    idserv.setText(String.valueOf(serv.getId()));
                    horasol.setText(serv.getFecha());
                    nameuser.setText(serv.getNombre());
                    tel.setText(String.valueOf(serv.getTel()));
                    mail.setText(serv.getEmail());
                    orig.setText(completeO);
                    dest.setText(completeD);
                    refer.setText(serv.getReferencia());

                    AcepRech=builder.create();
                    AcepRech.setView(promptDSS);
                    AcepRech.show();
                }

            }
        });

        return convertView;

    }


}
