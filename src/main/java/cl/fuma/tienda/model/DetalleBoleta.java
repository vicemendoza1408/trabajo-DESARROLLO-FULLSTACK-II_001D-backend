package cl.fuma.tienda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Tabla DETALLE_BOLETA. Cada fila es una línea de la boleta: un producto y su cantidad.
// Es el equivalente a un ítem del carrito del frontend.
@Entity
@Table(name = "detalle_boleta")
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muchos detalles -> una boleta.
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_boleta")
    private Boleta boleta;

    // Muchos detalles -> un producto.
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    // Precio final de UNA unidad (con descuento) en el momento de la compra.
    // Se guarda aquí para que la boleta no cambie si después se modifica el precio del producto.
    @Column(nullable = false)
    private int precioUnitario;

    public DetalleBoleta() {
    }

    public DetalleBoleta(Producto producto, int cantidad, int precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boleta getBoleta() {
        return boleta;
    }

    public void setBoleta(Boleta boleta) {
        this.boleta = boleta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(int precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
