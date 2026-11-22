package cl.fuma.tienda.exception;

// Se lanza cuando el correo o la contraseña del inicio de sesión no son correctos.
// El ManejadorErrores la convierte en una respuesta 401 (no autenticado).
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
