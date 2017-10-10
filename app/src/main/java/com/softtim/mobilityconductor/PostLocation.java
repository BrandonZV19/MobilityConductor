package com.softtim.mobilityconductor;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

/**
 * Created by softtim on 9/8/16.
 */
class PostLocation {
    private AsyncHttpClient client=new AsyncHttpClient();
    private RequestParams params=new RequestParams();
    private String LOG_TAG="PostLocation";



    void post(Location location, int idUnidad, Context context){
        String ip="softtim.mx/taxi";
        String url=context.getString(R.string.mainURL)+"/Servicios/agregar_ubicacion_sp";
        double lat=location.getLatitude();
        double lng=location.getLongitude();
        params.put("dLatitudRegistro",lat);
        params.put("dLongitudRegistro",lng);
        params.put("idUnidad",idUnidad);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG, "response:"+new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG," statusCode:"+statusCode);
            }
        });
    }
}
