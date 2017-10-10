package com.softtim.mobilityconductor;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by softtim on 9/11/16.
 */
public class Cifrar {

    static String toMD5(String cad){

        return encriptarHash(cad,"MD5");

    }

    private static String convierteAHexadecimal(byte[] digest){
        String hash = "";
        for(byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }
        return hash;
    }

    public static String encriptarHash(String message, String algorithm){
        byte[] digest = null;
        byte[] buffer = message.getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.reset();
            messageDigest.update(buffer);
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException ex) {
            Log.e("Cifrar","Error creando Digest");
        }
        return convierteAHexadecimal(digest);
    }

}