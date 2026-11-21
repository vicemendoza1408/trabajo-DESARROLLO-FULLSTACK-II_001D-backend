package cl.fuma.tienda.repository;

import cl.fuma.tienda.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

// Acceso a la tabla CATEGORIA.
// JpaRepository<Categoria, String>: Categoria es la entidad y String es el tipo de su id.
// Spring crea solo los métodos para guardar, buscar, listar y eliminar
// (save, findById, findAll, deleteById, count...). No hay que escribir SQL.
public interface CategoriaRepository extends JpaRepository<Categoria, String> {
}
