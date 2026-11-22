package cl.fuma.tienda.exception;

import java.util.Map;

// Formato en que el backend responde cuando algo sale mal. Ejemplo:
// { "estado": 400, "mensaje": "Datos inválidos: ...", "errores": { "precio": "El precio debe ser..." } }
// "errores" trae un mensaje por campo (vacío si el error no pertenece a un campo).
public record RespuestaError(int estado, String mensaje, Map<String, String> errores) {
}
