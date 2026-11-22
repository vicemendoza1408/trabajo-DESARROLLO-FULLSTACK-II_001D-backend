package cl.fuma.tienda.dto;

import cl.fuma.tienda.model.Producto;

// Lo que el backend devuelve al frontend por cada producto (en formato JSON).
// Tiene los mismos nombres de campo que ya usa el frontend (por ejemplo idCategoria).
//
// Un "record" es una clase de solo datos: Java crea solo el constructor y los getters.
public record ProductoRespuesta(
        Long id,
        String nombre,
        String descripcion,
        int precio,
        int descuentoPorcentaje,
        String idCategoria,
        String imagen) {

    // Convierte la entidad Producto (tabla) en la respuesta que se envía.
    public static ProductoRespuesta desde(Producto producto) {
        return new ProductoRespuesta(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getDescuentoPorcentaje(),
                producto.getCategoria().getId(),
                producto.getImagen());
    }
}
