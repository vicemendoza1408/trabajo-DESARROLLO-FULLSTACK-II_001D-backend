package cl.fuma.tienda.model;

// Roles del sistema (enunciado de la Evaluación 3):
// ADMIN    -> acceso total.
// VENDEDOR -> solo ve la lista y el detalle de productos y de órdenes (boletas).
// CLIENTE  -> solo usa la tienda.
public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE
}
