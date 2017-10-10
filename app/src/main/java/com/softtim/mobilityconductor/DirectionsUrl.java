package com.softtim.mobilityconductor;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

/**
 * Created by BrandonZamudio on 04/01/2016.
 */
class DirectionsUrl {
    String getUrl(LatLng origin, LatLng destination){
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String parameters = str_origin + "&" + str_dest;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters+"&departure_time=now&key=AIzaSyBL6VlVaw8DWU030cz2qRQLXnsSGjVdruI";
        Log.e("URL: ", url + "");
        return url;
    }
}
