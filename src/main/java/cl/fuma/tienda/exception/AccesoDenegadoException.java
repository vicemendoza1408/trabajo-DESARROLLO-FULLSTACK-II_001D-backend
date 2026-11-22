package cl.fuma.tienda.exception;

// Se lanza cuando un usuario SÍ inició sesión, pero no tiene permiso para lo que pide
// (por ejemplo, un cliente que intenta ver la boleta de otro cliente).
// El ManejadorErrores la convierte en una respuesta 403 (prohibido).
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
