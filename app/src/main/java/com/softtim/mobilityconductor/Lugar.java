package com.softtim.mobilityconductor;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by softtim on 3/27/17.
 */

public class Lugar extends RealmObject implements Serializable {
    @PrimaryKey
    private int id;

    private int idRuta;
    private int orden;
    private String nombre;
    private String descripcion, descripcionCorta;
    private String promo;
    private String imagen;
    private String horario;
    private String direccion;
    private String categoria;
    private double latitud;
    private double longitud;
    private String telefono;
    private String correo;
    private String sitio;

    public void setId(int id){
        this.id=id;
    }

    public int getId(){
        return id;
    }

    public void setIdRuta(int idRuta){
        this.idRuta=idRuta;
    }

    public int getIdRuta(){
        return idRuta;
    }

    public void setOrden(int orden){
        this.orden=orden;
    }

    public int getOrden(){
        return orden;
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
    }

    public String getNombre(){
        return nombre;
    }

    public void setDescripcionCorta(String descripcionCorta){
        this.descripcionCorta=descripcionCorta;
    }

    public String getDescripcionCorta(){
        return descripcionCorta;
    }

    public void setDescripcion(String descripcion){
        this.descripcion=descripcion;
    }

    public String getDescripcion(){
        return descripcion;
    }

    public void setPromo(String promo){
        this.promo=promo;
    }

    public String getPromo(){
        return promo;
    }

    public void setImagen(String imagen){
        this.imagen=imagen;
    }

    public String getImagen(){
        return imagen;
    }

    public void setHorario(String horario){
        this.horario=horario;
    }

    public String getHorario(){
        return horario;
    }

    public void setDireccion(String direccion){
        this.direccion=direccion;
    }

    public String getDireccion(){
        return direccion;
    }

    public void setCategoria(String categoria){
        this.categoria=categoria;
    }

    public String getCategoria(){
        return categoria;
    }

    public void setLatitud(double latitud){
        this.latitud=latitud;
    }

    public Double getLatitud(){
        return latitud;
    }

    public void setLongitud(double longitud){
        this.longitud=longitud;
    }

    public Double getLongitud(){
        return longitud;
    }

    public void setTelefono(String telefono){
        this.telefono=telefono;
    }

    public String getTelefono(){
        return telefono;
    }

    public void setSitio(String sitio){
        this.sitio=sitio;
    }

    public String getSitio(){
        return sitio;
    }

    public void setCorreo(String correo){
        this.correo=correo;
    }

    public String getCorreo(){
        return correo;
    }

}
