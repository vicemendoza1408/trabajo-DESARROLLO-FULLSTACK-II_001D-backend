package cl.fuma.tienda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla CATEGORIA. El id es un texto corto ("poleras", "polerones"...), igual que en el frontend,
// así las rutas /categorias/:idCategoria no cambian.
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @Column(length = 30)
    private String id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    // JPA necesita un constructor vacío.
    public Categoria() {
    }

    public Categoria(String id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
}
