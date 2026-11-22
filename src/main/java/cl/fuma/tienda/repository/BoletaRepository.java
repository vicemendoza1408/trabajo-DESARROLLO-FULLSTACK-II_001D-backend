package cl.fuma.tienda.repository;

import cl.fuma.tienda.model.Boleta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Acceso a la tabla BOLETA (sus detalles se guardan junto con ella).
// JpaRepository<Boleta, Long>: Boleta es la entidad y Long es el tipo de su id.
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    // Todas las boletas, de la más nueva a la más antigua.
    // Si dos boletas tienen la misma fecha (MySQL guarda la fecha sin fracciones de segundo),
    // se desempata por id descendente, así la última creada siempre queda primero.
    List<Boleta> findAllByOrderByFechaDescIdDesc();

    // Las boletas de un usuario, de la más nueva a la más antigua (mismo desempate por id).
    List<Boleta> findByUsuarioIdOrderByFechaDescIdDesc(Long idUsuario);
}
