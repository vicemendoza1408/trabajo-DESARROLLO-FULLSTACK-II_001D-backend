package cl.fuma.tienda.util;

// Cálculos de precios. Es el equivalente en Java de utils/precios.js del frontend.
// Son métodos "puros": solo reciben números y devuelven un resultado, sin tocar la base de datos.
public final class Precios {

    // Opciones de entrega y costo fijo del despacho, en pesos chilenos.
    public static final String ENTREGA_RETIRO = "retiro";
    public static final String ENTREGA_DESPACHO = "despacho";
    public static final int COSTO_DESPACHO = 3990;

    private Precios() {
        // Clase de utilidades: no se crean objetos de ella.
    }

    // Precio con descuento, redondeado a pesos enteros.
    // Ejemplo: precio 24990 con 20 % de descuento -> 19992.
    public static int calcularPrecioFinal(int precio, int descuentoPorcentaje) {
        return (int) Math.round(precio * (100 - descuentoPorcentaje) / 100.0);
    }

    // El despacho cuesta COSTO_DESPACHO y el retiro en tienda es gratis.
    public static int calcularCostoEnvio(String opcionEntrega) {
        return ENTREGA_DESPACHO.equals(opcionEntrega) ? COSTO_DESPACHO : 0;
    }
}
