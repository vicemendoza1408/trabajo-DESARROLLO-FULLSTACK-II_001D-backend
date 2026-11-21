package cl.fuma.tienda.exception;

import java.util.HashMap;
import java.util.Map;

// Se lanza cuando los datos recibidos no cumplen las reglas del negocio
// (por ejemplo, un precio negativo o un correo mal escrito).
// En la Fase 3 se convertirá en una respuesta HTTP 400 (Solicitud inválida).
//
// "errores" funciona igual que el objeto de errores del frontend (validaciones.js):
// la clave es el campo y el valor es el mensaje, por ejemplo {"precio": "El precio debe ser mayor que 0."}.
public class DatosInvalidosException extends RuntimeException {

    private final Map<String, String> errores;

    // Para un error general que no pertenece a un campo.
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
        this.errores = new HashMap<>();
    }

    // Para errores por campo. El mensaje general junta todos los mensajes.
    public DatosInvalidosException(Map<String, String> errores) {
        super("Datos inválidos: " + String.join(" ", errores.values()));
        this.errores = errores;
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
