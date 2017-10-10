package com.softtim.mobilityconductor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;



import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RecargarSaldo extends AppCompatActivity {
    WebView webView;
    SharedPreferences preferences;
    String idConductor;
    Button hecho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recargar_saldo);

        preferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        if (!preferences.contains(getString(R.string.p_id_conductor))){
            Snackbar.make(getWindow().getDecorView().getRootView(), "Error 404", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            Intent intentm = new Intent(RecargarSaldo.this, MainActivity.class);
            startActivity(intentm);
            finish();
        }else {
            idConductor = String.valueOf(preferences.getInt((getString(R.string.p_id_conductor)),0));
            webView=(WebView)findViewById(R.id.webRecargar);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());

            String url = getString(R.string.mainURL)+"/Conductor/recargar_saldo";
            String postData="";
            try {
                postData = "idConductor=" + URLEncoder.encode(idConductor,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            webView.postUrl(url,postData.getBytes());
        }

        hecho=(Button)findViewById(R.id.hechoR);
        hecho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentm = new Intent(RecargarSaldo.this, MainActivity.class);
                startActivity(intentm);
                finish();
            }
        });

    }

}
