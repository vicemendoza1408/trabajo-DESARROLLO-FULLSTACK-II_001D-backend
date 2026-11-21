package cl.fuma.tienda.repository;

import cl.fuma.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

// Acceso a la tabla PRODUCTO.
// JpaRepository<Producto, Long>: Producto es la entidad y Long es el tipo de su id.
// Spring crea solo los métodos para guardar, buscar, listar y eliminar
// (save, findById, findAll, deleteById, count...). No hay que escribir SQL.
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
