package cl.fuma.tienda.dto;

// Datos que envía el panel de administración para crear o editar un producto.
// Es lo mismo que escribe el administrador en el formulario del frontend.
// DTO = objeto que solo transporta datos entre el frontend y el backend.
public class SolicitudProducto {

    private String nombre;
    private String descripcion;
    private int precio;
    private int descuentoPorcentaje;
    private String imagen;
    // Id de la categoría, por ejemplo "poleras".
    private String idCategoria;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public int getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(int descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }
}
