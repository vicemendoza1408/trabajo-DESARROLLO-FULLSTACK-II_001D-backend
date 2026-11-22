package cl.fuma.tienda.dto;

import cl.fuma.tienda.model.Boleta;
import cl.fuma.tienda.model.DetalleBoleta;
import java.time.LocalDateTime;
import java.util.List;

// Lo que el backend devuelve al frontend por cada boleta (el "pedido" de la tienda).
// Tiene la misma forma que el pedido que ya usa el frontend: cliente, items, subtotal, costoEnvio y total.
// No incluye datos del usuario como su contraseña: solo lo que la pantalla necesita mostrar.
public record BoletaRespuesta(
        Long id,
        LocalDateTime fecha,
        Cliente cliente,
        String direccion,
        String opcionEntrega,
        List<Item> items,
        int subtotal,
        int costoEnvio,
        int total) {

    // Datos de contacto escritos en el checkout.
    public record Cliente(String nombre, String correo, String telefono) {
    }

    // Una línea de la boleta (un producto y su cantidad).
    public record Item(Long idProducto, String nombre, String imagen, int precioUnitario, int cantidad) {
    }

    // Convierte la entidad Boleta (tabla) en la respuesta que se envía.
    public static BoletaRespuesta desde(Boleta boleta) {
        List<Item> items = boleta.getDetalles().stream()
                .map(BoletaRespuesta::convertirDetalle)
                .toList();

        return new BoletaRespuesta(
                boleta.getId(),
                boleta.getFecha(),
                new Cliente(boleta.getNombreCliente(), boleta.getCorreoCliente(), boleta.getTelefonoCliente()),
                boleta.getDireccion(),
                boleta.getOpcionEntrega(),
                items,
                boleta.getSubtotal(),
                boleta.getCostoEnvio(),
                boleta.getTotal());
    }

    private static Item convertirDetalle(DetalleBoleta detalle) {
        return new Item(
                detalle.getProducto().getId(),
                detalle.getProducto().getNombre(),
                detalle.getProducto().getImagen(),
                detalle.getPrecioUnitario(),
                detalle.getCantidad());
    }
}
