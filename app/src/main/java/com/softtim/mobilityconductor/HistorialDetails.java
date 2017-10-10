package com.softtim.mobilityconductor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class HistorialDetails extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    Servicio servicio;
    String TAG="Details";
    TextView nombre,origen,destino,fecha,costo,idserv,tipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_details);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDH);
        mapFragment.getMapAsync(this);

        nombre=(TextView)findViewById(R.id.dhNombre);
        origen=(TextView)findViewById(R.id.dhOrigen);
        destino=(TextView)findViewById(R.id.dhDestino);
        fecha=(TextView)findViewById(R.id.dhFecha);
        costo=(TextView)findViewById(R.id.dhCosto);
        idserv=(TextView)findViewById(R.id.dhID);
        tipo=(TextView)findViewById(R.id.dhTipo);

        if (getIntent().getSerializableExtra("serv")!=null){
            servicio= (Servicio) getIntent().getSerializableExtra("serv");

            nombre.setText(servicio.getNombre());
            origen.setText(servicio.getDir_orig());
            destino.setText(servicio.getDir_dest());
            fecha.setText(servicio.getFecha());
            costo.setText(String.valueOf(servicio.getCostoCond()));
            idserv.setText(String.valueOf(servicio.getId()));

            if (servicio.getTipo_pago()!=null) {
                if (servicio.getTipo_pago().contains("PayPal")) {
                    tipo.setText("Pagado con PayPal");
                } else {
                    tipo.setText("Pagado en efectivo");
                }
            }

        }else {
            Log.e(TAG,"null closed");
            finish();
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

        double lato=servicio.getLat_o();
        double latd=servicio.getLat_d();
        double lngo=servicio.getLng_o();
        double lngd=servicio.getLng_d();

        LatLng orig=new LatLng(lato,lngo);
        LatLng dest=new LatLng(latd,lngd);

        mMap.addMarker(new MarkerOptions()
                .position(orig)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.markera))
                .title("Origen")
                .draggable(true));

        mMap.addMarker(new MarkerOptions()
                .position(dest)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.markerb))
                .title("Destino")
                .draggable(true));
    }

}
