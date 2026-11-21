package cl.fuma.tienda.dto;

import java.util.ArrayList;
import java.util.List;

// Datos que envía el checkout del frontend para comprar: contacto, entrega, tarjeta y productos.
// El número de tarjeta solo se usa para el pago simulado y NUNCA se guarda.
public class SolicitudBoleta {

    private String nombre;
    private String correo;
    private String telefono;
    // "retiro" o "despacho".
    private String opcionEntrega;
    // Solo es obligatoria cuando la entrega es "despacho".
    private String direccion;
    private String numeroTarjeta;
    private List<ItemSolicitud> items = new ArrayList<>();

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getOpcionEntrega() {
        return opcionEntrega;
    }

    public void setOpcionEntrega(String opcionEntrega) {
        this.opcionEntrega = opcionEntrega;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public List<ItemSolicitud> getItems() {
        return items;
    }

    public void setItems(List<ItemSolicitud> items) {
        this.items = items;
    }
}
