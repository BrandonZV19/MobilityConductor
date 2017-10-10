package com.softtim.mobilityconductor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    Button iniciar;
    EditText user,pass;
    SharedPreferences preferences;
    String LOG_TAG="ULoginActivity";
    String currentIdCelular, aToken;
    ProgressDialog progressDialog;
    AlertDialog dialogCampoVacio,dialogError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.simplebar);
        toolbar.setTitle(" Conductores");
        setSupportActionBar(toolbar);

        Log.e(LOG_TAG,"onCreateToken: "+ FirebaseInstanceId.getInstance().getToken());

        aToken=FirebaseInstanceId.getInstance().getToken();

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.accediendo));

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

                 if (progressDialog!=null && progressDialog.isShowing()) {             progressDialog.dismiss();             Log.e("dismiss in","c stat before");         }          progressDialog=null;          if(LoginActivity.this.getParent()!=null){             progressDialog=new ProgressDialog(LoginActivity.this.getParent());         }else{             progressDialog=new ProgressDialog(LoginActivity.this);         }          progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);         progressDialog.setCancelable(false);         progressDialog.setTitle("Loading...");          if(!LoginActivity.this.isFinishing()) {             progressDialog.show();             Log.e("show in","");         }

        final String passMD5=Cifrar.toMD5(pass.getText().toString().trim());
        final String usuario=user.getText().toString().trim();
        //guardar
         currentIdCelular=Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.put("aUsuario",usuario);
        params.put("aPassword",passMD5);
        params.put("idCelular",currentIdCelular);
        params.put("aToken",aToken);

        Log.e(LOG_TAG,"Usuario:"+user.getText().toString().trim()+
                " Password:"+passMD5+" ID:"+currentIdCelular +"Token:"+aToken);

        client.post(getString(R.string.mainURL)+"/Inicio/login_conductor", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG,"response:"+ new String(responseBody));
                try {
                    JSONObject jsonObject=new JSONObject(new String(responseBody));
                    String respuesta=jsonObject.getString("login");
                    if (respuesta.contains("1")){



                        //Get datos del json
                        int idConductor=jsonObject.getInt("idConductor");
                        int idUnidad=jsonObject.getInt("idUnidad");
                        int lOcupado=jsonObject.getInt("lOcupado");
                        int idTipoUnidad=jsonObject.getInt("idTipoUnidad"); //1 taxi 2 clasea 3 claseb
                        String tipoUnidad="";

                        if (idTipoUnidad==1){
                            tipoUnidad="Taxi";
                        }else if (idTipoUnidad==2){
                            tipoUnidad="Clase A";
                        }else if (idTipoUnidad==3){
                            tipoUnidad="Clase B";
                        }

                        //Guardar datos de usuario en preferencias
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString(getString(R.string.p_pass),passMD5);
                        editor.putInt(getString(R.string.p_id_conductor),idConductor);
                        editor.putInt(getString(R.string.p_id_unidad),idUnidad);
                        //editor.putString(getString(R.string.p_username),);
                        editor.putString(getString(R.string.p_tipo_unidad),tipoUnidad);
                        editor.putInt("active",1);
                        editor.putInt(getString(R.string.current_ocupado),lOcupado);
                        editor.putString(getString(R.string.p_nick),usuario);
                        editor.commit();

                        //Que paso con la respuesta String res = vec[2]; //conducto o usuario
                        //falta saldo del panico dSaldoConductor = vec[5];
                        //Falta el string deshabilitado String deshabilitado = vec[6];



                        Intent intent= new Intent(LoginActivity.this, MainActivity.class);
                        progressDialog.dismiss();
                        startActivity(intent);
                        finish();
                    }else {
                        //Falta validar el por que la respuesta no contiene 1 y que significa
                        //Falta Sesion activa

                        String idCelular=jsonObject.getString("idCelular");
                        int lPasswordTemporal=jsonObject.getInt("lPasswordTemporal");

                        if (lPasswordTemporal==1){
                            //cambiar contraseña
                        }

                        if (idCelular!=currentIdCelular){
                            //No es
                        }

                        if (dialogError!=null && dialogError.isShowing()){
                            dialogError.dismiss();
                        }
                        dialogError=null; //Antes de crear una nueva vista del dialog lo ponemos como null
                        dialogError=new AlertDialog.Builder(LoginActivity.this)
                                .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setMessage(getString(R.string.error_usuario_pass))
                                .show();
                        progressDialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"response:"+ new String(responseBody)+" statusCode:"+statusCode);
                if (dialogError!=null && dialogError.isShowing()){
                    dialogError.dismiss();
                }
                dialogError=null; //Antes de crear una nueva vista del dialog lo ponemos como null
                dialogError=new AlertDialog.Builder(LoginActivity.this)
                        .setNeutralButton(getString(R.string.string_aceptar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setMessage(getString(R.string.error_conection)+" "+statusCode)
                        .show();
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
