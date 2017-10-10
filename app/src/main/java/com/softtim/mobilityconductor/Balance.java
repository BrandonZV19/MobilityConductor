package com.softtim.mobilityconductor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Balance extends AppCompatActivity {
    Button recargar;
    SharedPreferences preferences;
    TextView saldoT;
    float currentSaldo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        preferences=getApplicationContext().getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);

        saldoT=(TextView)findViewById(R.id.tvSaldo);

        if (preferences.contains(getString(R.string.current_saldo))){
            currentSaldo=preferences.getFloat(getString(R.string.current_saldo), (float) 0.0);
            saldoT.setText( String.valueOf(currentSaldo) );
        }else {
            saldoT.setText("--.--");
        }

        if (currentSaldo<16){
            saldoT.setTextColor(Color.parseColor("#d50000"));
        }

        recargar=(Button)findViewById(R.id.bbRecargar);
        recargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentS = new Intent(Balance.this, RecargarSaldo.class);
                startActivity(intentS);
            }
        });

    }
}
