package cl.fuma.tienda.exception;

// Se lanza cuando el pago simulado es rechazado (tarjeta que empieza con 0000).
// Cuando ocurre, NO se crea ninguna boleta.
public class PagoRechazadoException extends RuntimeException {

    public PagoRechazadoException(String mensaje) {
        super(mensaje);
    }
}
