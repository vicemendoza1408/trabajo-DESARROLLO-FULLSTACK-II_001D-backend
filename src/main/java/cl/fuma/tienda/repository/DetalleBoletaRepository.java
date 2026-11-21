package cl.fuma.tienda.repository;

import cl.fuma.tienda.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

// Acceso a la tabla DETALLE_BOLETA.
// JpaRepository<DetalleBoleta, Long>: DetalleBoleta es la entidad y Long es el tipo de su id.
public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    // Responde si algún detalle de boleta usa ese producto (es decir, si el producto ya se vendió).
    boolean existsByProductoId(Long idProducto);
}
