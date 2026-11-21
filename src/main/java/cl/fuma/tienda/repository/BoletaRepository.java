package cl.fuma.tienda.repository;

import cl.fuma.tienda.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

// Acceso a la tabla BOLETA (sus detalles se guardan junto con ella).
// JpaRepository<Boleta, Long>: Boleta es la entidad y Long es el tipo de su id.
// Spring crea solo los métodos para guardar, buscar, listar y eliminar
// (save, findById, findAll, deleteById, count...). No hay que escribir SQL.
public interface BoletaRepository extends JpaRepository<Boleta, Long> {
}
