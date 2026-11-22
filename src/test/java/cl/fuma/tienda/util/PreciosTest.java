package cl.fuma.tienda.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// Pruebas unitarias de los cálculos de precios. No usan Spring ni base de datos.
// Los resultados esperados son los mismos que entrega utils/precios.js en el frontend.
class PreciosTest {

    @Test
    void productoSinDescuentoMantieneSuPrecio() {
        assertEquals(29990, Precios.calcularPrecioFinal(29990, 0));
    }

    @Test
    void aplicaDescuentoDel20PorCiento() {
        assertEquals(19992, Precios.calcularPrecioFinal(24990, 20));
    }

    @Test
    void aplicaDescuentoDel30PorCiento() {
        assertEquals(19593, Precios.calcularPrecioFinal(27990, 30));
    }

    @Test
    void redondeaAPesosEnterosHaciaArribaEnLaMitad() {
        // 12990 con 15 % de descuento = 11041,5 -> se redondea a 11042.
        assertEquals(11042, Precios.calcularPrecioFinal(12990, 15));
    }

    @Test
    void descuentoDel100PorCientoDejaElPrecioEnCero() {
        assertEquals(0, Precios.calcularPrecioFinal(9990, 100));
    }

    @Test
    void despachoCuesta3990() {
        assertEquals(3990, Precios.calcularCostoEnvio("despacho"));
    }

    @Test
    void retiroEnTiendaEsGratis() {
        assertEquals(0, Precios.calcularCostoEnvio("retiro"));
    }

    @Test
    void opcionDeEntregaDesconocidaNoCobraEnvio() {
        assertEquals(0, Precios.calcularCostoEnvio("paloma-mensajera"));
    }
}
