package com.brandonzamudio.softtim.mobilityconductor;

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
public class PostLocation {
    AsyncHttpClient client=new AsyncHttpClient();
    RequestParams params=new RequestParams();
    String LOG_TAG="PostLocation";

    public void post(Location location, int idUnidad){
        double lat=location.getLatitude();
        double lng=location.getLongitude();
        params.put("dLatitudRegistro",lat);
        params.put("dLongitudRegistro",lng);
        params.put("fFechaHoraSP",System.currentTimeMillis());
        params.put("idUnidad",idUnidad);
        client.post(String.valueOf(R.string.ip), params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(LOG_TAG, "response:"+Arrays.toString(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(LOG_TAG,"response:"+ Arrays.toString(responseBody)+" statusCode:"+statusCode);
            }
        });
    }
}
