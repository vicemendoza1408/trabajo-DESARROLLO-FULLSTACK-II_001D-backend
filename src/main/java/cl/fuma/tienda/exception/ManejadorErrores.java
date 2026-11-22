package cl.fuma.tienda.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Convierte las excepciones del negocio en respuestas HTTP con un código claro.
// @RestControllerAdvice hace que esta clase atrape los errores de TODOS los controladores,
// así los controladores no necesitan try/catch.
@RestControllerAdvice
public class ManejadorErrores {

    // El producto, la categoría o la boleta no existe -> 404.
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoException error) {
        return responder(HttpStatus.NOT_FOUND, error.getMessage(), Map.of());
    }

    // Los datos no cumplen las reglas (precio negativo, correo inválido...) -> 400.
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<RespuestaError> manejarDatosInvalidos(DatosInvalidosException error) {
        return responder(HttpStatus.BAD_REQUEST, error.getMessage(), error.getErrores());
    }

    // El pago simulado fue rechazado -> 402 (Pago requerido).
    // Usa un código propio para que el frontend distinga "pago rechazado" de "datos inválidos".
    @ExceptionHandler(PagoRechazadoException.class)
    public ResponseEntity<RespuestaError> manejarPagoRechazado(PagoRechazadoException error) {
        return responder(HttpStatus.PAYMENT_REQUIRED, error.getMessage(), Map.of());
    }

    // El cuerpo de la petición no es un JSON válido (o un número viene como texto) -> 400.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarJsonInvalido(HttpMessageNotReadableException error) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido.", Map.of());
    }

    // Un parámetro de la URL tiene el tipo equivocado (por ejemplo /productos/abc) -> 400.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> manejarTipoIncorrecto(MethodArgumentTypeMismatchException error) {
        return responder(HttpStatus.BAD_REQUEST, "El valor del parámetro '" + error.getName() + "' no es válido.",
                Map.of());
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje, Map<String, String> errores) {
        return ResponseEntity.status(estado).body(new RespuestaError(estado.value(), mensaje, errores));
    }
}
