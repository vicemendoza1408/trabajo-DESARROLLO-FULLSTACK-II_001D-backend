package cl.fuma.tienda.dto;

// Una línea de la compra que envía el frontend: qué producto y cuántas unidades.
// Solo viajan el id y la cantidad: el precio NO se envía, lo busca el servidor en la base de datos.
public class ItemSolicitud {

    private Long idProducto;
    private int cantidad;

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
