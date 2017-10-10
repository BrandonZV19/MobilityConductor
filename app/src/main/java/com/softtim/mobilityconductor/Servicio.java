package com.softtim.mobilityconductor;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by softtim on 9/27/16.

 int id=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_id)));
 String ap_mat=remoteMessage.getData().get(getString(R.string.p_s_apellido_mat));
 String ap_pat=remoteMessage.getData().get(getString(R.string.p_s_apellido_pat));
 int cancelado=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_cancelado)));
 int cancelado_v=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_cancelado_visto)));
 int confirmado=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_confirmado)));
 int descartado=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_descartado)));
 String email=remoteMessage.getData().get(getString(R.string.p_s_email));
 String fecha=remoteMessage.getData().get(getString(R.string.p_s_fecha));
 float lat_d=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lat_destino)));
 float lng_d=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lng_destino)));
 float lat_o=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lat_origen)));
 float lng_o=Float.parseFloat(remoteMessage.getData().get(getString(R.string.p_s_lng_origen)));
 String nombre=remoteMessage.getData().get(getString(R.string.p_s_nombre));
 String referencia=remoteMessage.getData().get(getString(R.string.p_s_referencia));
 int status=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_status)));
 int tel=Integer.parseInt(remoteMessage.getData().get(getString(R.string.p_s_telefono)));

 db.execSQL("CREATE TABLE RegistrosServicios(IDServicio INTEGER PRIMARY KEY, HoraSolicitud TEXT, UserName TEXT," +
 "Telefono TEXT, Email TEXT, Origen TEXT, Destino TEXT, Referencia TEXT, Monto INTEGER)");

 */

public class Servicio extends RealmObject implements Serializable{

    @PrimaryKey
    private int id;

    private String ap_mat;
    private String ap_pat;
    private int cancelado;
    private int cancelado_v;
    private int confirmado;
    private int descartado;
    private String email;
    private String fecha;
    private double lat_d;
    private double lng_d;
    private double lat_o;
    private double lng_o;
    private double calif_serv;
    private double calif_user;
    private String dir_dest;
    private String dir_orig;
    private String nombre;
    private String referencia;
    private String colonia_orig;
    private String colonia_dest;
    private int status;
    private String tel;
    private double costo_s;
    private double costo_u;
    private double costo_c;
    private int pagado;

    public String getTipo_pago() {
        return tipo_pago;
    }

    public void setTipo_pago(String tipo_pago) {
        this.tipo_pago = tipo_pago;
    }

    private String tipo_pago;

    //Agregar colonia de origen y colonia de destino

    public void setId(int id){
        this.id=id;
    }

    public int getId(){
        return id;
    }

    public void setAp_mat(String ap_mat){
        this.ap_mat=ap_mat;
    }

    public String getAp_mat(){
        return ap_mat;
    }

    public void setAp_pat(String ap_pat){
        this.ap_pat=ap_pat;
    }

    public String getAp_pat(){
        return ap_pat;
    }

    public void setCancelado(int cancelado){
        this.cancelado=cancelado;
    }

    public int getCancelado(){
        return cancelado;
    }

    public void setCancelado_v(int cancelado_v){
        this.cancelado_v=cancelado_v;
    }

    public int getCancelado_v(){
        return cancelado_v;
    }

    public void setConfirmado(int confirmado){
        this.confirmado=confirmado;
    }

    public int getConfirmado(){
        return confirmado;
    }

    public void setDescartado(int descartado){
        this.descartado=descartado;
    }

    public int getDescartado(){
        return descartado;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public String getEmail(){
        return email;
    }

    public void setFecha(String fecha){
        this.fecha=fecha;
    }

    public String getFecha(){
        return fecha;
    }

    public void setLat_d(double lat_d){
        this.lat_d=lat_d;
    }

    public double getLat_d(){
        return lat_d;
    }

    public void setLng_d(double lng_d){
        this.lng_d=lng_d;
    }

    public double getLng_d(){
        return lng_d;
    }

    public void setLat_o(double lat_o){
        this.lat_o=lat_o;
    }

    public double getLat_o(){
        return lat_o;
    }

    public void setLng_o(double lng_o){
        this.lng_o=lng_o;
    }

    public double getLng_o(){
        return lng_o;
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
    }

    public String getNombre(){
        return nombre;
    }

    public void setReferencia(String referencia){
        this.referencia=referencia;
    }

    public String getReferencia(){
        return referencia;
    }

    public void setDir_dest(String dir_dest){
        this.dir_dest=dir_dest;
    }

    public String getDir_orig(){
        return dir_orig;
    }

    public void setDir_orig(String dir_orig){
        this.dir_orig=dir_orig;
    }

    public String getDir_dest(){
        return dir_dest;
    }

    public void setColonia_orig(String colonia_orig){
        this.colonia_orig=colonia_orig;
    }

    public String getColonia_orig(){
        return colonia_orig;
    }

    public void setColonia_dest(String colonia_dest){
        this.colonia_dest=colonia_dest;
    }

    public String getColonia_dest(){
        return colonia_dest;
    }

    public void setStatus(int status){
        this.status=status;
    }

    public int getStatus(){
        return status;
    }

    public void setTel(String tel){
        this.tel=tel;
    }

    public String getTel(){
        return tel;
    }

    public void setCostoSug(double costo_s){
        this.costo_s=costo_s;
    }

    public double getCostoSug(){
        return costo_s;
    }

    public void setCostoUs(double costo_u){
        this.costo_u=costo_u;
    }

    public double getCostoUs(){
        return costo_u;
    }

    public void setCostoCond(double costo_c){
        this.costo_c=costo_c;
    }

    public double getCostoCond(){
        return costo_c;
    }

    public void setPagado(int pagado){
        this.pagado=pagado;
    }

    public int getPagado(){
        return pagado;
    }

    public void setCalif_serv(double calif_serv){
        this.calif_serv=calif_serv;
    }

    public double getCalif_serv(){
        return calif_serv;
    }

    public void setCalif_user(double calif_user){
        this.calif_user=calif_user;
    }

    public double getCalif_user(){
        return calif_user;
    }

}
