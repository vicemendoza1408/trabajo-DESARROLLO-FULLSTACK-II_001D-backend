package cl.fuma.tienda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Tabla PRODUCTO. Cada producto pertenece a una categoría (relación muchos a uno).
@Entity
@Table(name = "producto")
public class Producto {

    // El id lo genera la base de datos (autoincremental).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    // Precio original en pesos chilenos, sin decimales.
    @Column(nullable = false)
    private int precio;

    // 0 significa que el producto no está en oferta.
    @Column(nullable = false)
    private int descuentoPorcentaje;

    // Ruta de la imagen. El archivo vive en la carpeta public/img del frontend.
    @Column(length = 200)
    private String imagen;

    // Muchos productos -> una categoría. En la tabla se guarda la columna id_categoria.
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    public Producto() {
    }

    public Producto(String nombre, String descripcion, int precio, int descuentoPorcentaje,
                    String imagen, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.imagen = imagen;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
