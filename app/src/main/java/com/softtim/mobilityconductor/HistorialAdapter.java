package com.softtim.mobilityconductor;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.List;

/**
 * Created by softtim on 5/17/17.
 */

public class HistorialAdapter extends ArrayAdapter<Servicio> {
    private Context ctx;
    private int resource;
    private LayoutInflater inflater;

    public HistorialAdapter(@NonNull Context context, @LayoutRes int resourceid, @NonNull List<Servicio> objects) {
        super(context, resourceid, objects);
        resource = resourceid;
        inflater = LayoutInflater.from(context);
        ctx=context;
        Log.e("ServiciosAdapter", " lenght " + objects.size());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        convertView = inflater.inflate(resource, null);
        final Servicio serv = getItem(position);

        assert serv != null;
        String user=serv.getNombre();
        String orig=serv.getColonia_orig();
        String dest=serv.getColonia_dest();
        String date=serv.getFecha();

        /* USER IMAGE CAN BE
        ImageView imageView=(ImageView)convertView.findViewById(R.id.rowServiciosImage);
        Picasso.with(ctx).load("http://softtim.mx/canaco/assets/img/servs/"+img).into(imageView);
        */

        TextView nombre= (TextView) convertView.findViewById(R.id.historialRowUser);
        nombre.setText(user);

        TextView origen= (TextView) convertView.findViewById(R.id.historialRowOrigen);
        origen.setText(orig);

        TextView destino= (TextView) convertView.findViewById(R.id.historialRowDestino);
        destino.setText(dest);

        TextView fecha= (TextView) convertView.findViewById(R.id.historialRowDate);
        fecha.setText(date);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HistorialAdapter", "onclick");
                Intent intent=new Intent(ctx,HistorialDetails.class);
                intent.putExtra("serv",serv);
                ctx.startActivity(intent);
            }
        });

        return convertView;
    }

}