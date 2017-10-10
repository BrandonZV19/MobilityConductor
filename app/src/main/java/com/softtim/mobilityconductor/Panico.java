package com.softtim.mobilityconductor;

import java.io.Serializable;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Anahi on 23/05/2017.
 */
public class Panico extends RealmObject implements Serializable {

    @PrimaryKey
    private int IDalerta;

    private String fecha;
    private String nombre;
    private String marca;
    private String modelo;
    private String placas;
    private String imagenC;
    private String imagenU;
    private double latitud;
    private double longitud;

    public int getIDalerta() {
        return IDalerta;
    }

    public void setIDalerta(int IDalerta) {
        this.IDalerta = IDalerta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getPlacas() {
        return placas;
    }

    public void setPlacas(String placas) {
        this.placas = placas;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public String getImagenC() {
        return imagenC;
    }

    public void setImagenC(String imagenC) {
        this.imagenC = imagenC;
    }

    public String getImagenU() {
        return imagenU;
    }

    public void setImagenU(String imagenU) {
        this.imagenU = imagenU;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }




}
