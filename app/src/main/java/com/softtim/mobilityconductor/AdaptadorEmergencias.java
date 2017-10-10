package com.softtim.mobilityconductor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Anahi on 23/05/2017.
 */
class AdaptadorEmergencias extends ArrayAdapter<Panico>{
    private Context context;

    public AdaptadorEmergencias(Context contextx, List<Panico> panico) {
        super(contextx, R.layout.row_emergencias, panico);
        context=contextx;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View item = inflater.inflate(R.layout.row_emergencias, null);

        ImageView imagenC = (ImageView) item.findViewById(R.id.egConductor);
        TextView nombreC = (TextView) item.findViewById(R.id.egNombre);
        TextView modeloMarcaC = (TextView) item.findViewById(R.id.egMarcaModelo);
        TextView placasC = (TextView) item.findViewById(R.id.egPlacas);
        TextView fechaC = (TextView) item.findViewById(R.id.egFecha);

        final Panico panicoItem = getItem(position);

        if (panicoItem != null) {
            String marcaModeloC= panicoItem.getMarca()+""+ panicoItem.getModelo();
            nombreC.setText(panicoItem.getNombre());
            modeloMarcaC.setText(marcaModeloC);
            placasC.setText(panicoItem.getPlacas());
            fechaC.setText(panicoItem.getFecha());
            Picasso.with(context).load("http://softtim.mx/mobilityV2/assets/img/conductores/"+ panicoItem.getImagenC()).into(imagenC);
            //Picasso.with(context).load("http://softtim.mx/mobilityV2/assets/img/unidades/"+ panicoItem.getImagenU()).into(imagenU);
        }

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MapaEmergencia.class);
                intent.putExtra("panic", panicoItem);

                context.startActivity(intent);
            }
        });
        return item;
    }

}
