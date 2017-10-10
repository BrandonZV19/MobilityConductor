package com.softtim.mobilityconductor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

/**
 * Created by softtim on 9/14/16.
 */
class PostToken {
    private AsyncHttpClient client=new AsyncHttpClient();
    private RequestParams params=new RequestParams();
    private String TAG="PostToken";

    void post(String token, Context context){
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);
        String pass= preferences.getString(context.getString(R.string.p_pass),null);
        String user= preferences.getString(context.getString(R.string.p_username),null);

        String url="http://softtim.mx/taxi/taxi/MapaEmergencia/cambio_token_conductor";

        //Mandar encriptado
        //Validar valores nulos de los parametros a postear

        params.put("a_usuario_app",user);
        params.put("a_password_app",pass);
        params.put("a_token_app",token);
        Log.e(TAG, "params:"+ params.toString());
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(TAG, "response:"+ Arrays.toString(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG,"response:"+ Arrays.toString(responseBody)+" statusCode:"+statusCode);
            }
        });
    }
}
