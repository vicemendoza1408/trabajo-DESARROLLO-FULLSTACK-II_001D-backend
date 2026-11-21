package cl.fuma.tienda.repository;

import cl.fuma.tienda.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Acceso a la tabla PRODUCTO.
// JpaRepository<Producto, Long>: Producto es la entidad y Long es el tipo de su id.
// Spring crea solo los métodos básicos (save, findById, findAll, deleteById...).
// Además, Spring entiende el NOMBRE de los métodos de abajo y arma la consulta SQL por nosotros.
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // "findByCategoriaId" -> productos cuya categoría tiene ese id.
    List<Producto> findByCategoriaId(String idCategoria);

    // "findByDescuentoPorcentajeGreaterThan" -> productos con descuento mayor que el número dado.
    // Con 0 devuelve las ofertas (los productos con algún descuento).
    List<Producto> findByDescuentoPorcentajeGreaterThan(int descuento);
}
