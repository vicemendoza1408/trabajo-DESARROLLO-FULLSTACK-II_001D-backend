package cl.fuma.tienda.repository;

import cl.fuma.tienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

// Acceso a la tabla USUARIO.
// JpaRepository<Usuario, Long>: Usuario es la entidad y Long es el tipo de su id.
// Spring crea solo los métodos para guardar, buscar, listar y eliminar
// (save, findById, findAll, deleteById, count...). No hay que escribir SQL.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
