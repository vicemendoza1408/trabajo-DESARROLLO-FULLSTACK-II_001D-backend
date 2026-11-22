package cl.fuma.tienda.security;

import cl.fuma.tienda.exception.RespuestaError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

// Responde en JSON cuando Spring Security bloquea una petición.
// Estos errores ocurren ANTES de llegar a los controladores, por eso no los atrapa el ManejadorErrores.
//   - 401: no hay token válido (no iniciaste sesión o el token venció).
//   - 403: iniciaste sesión, pero tu rol no tiene permiso para ese endpoint.
@Component
public class ManejadorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ManejadorSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 401: se llama cuando se pide un endpoint protegido sin estar autenticado.
    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
                         AuthenticationException error) throws IOException {
        escribir(respuesta, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para acceder a este recurso.");
    }

    // 403: se llama cuando el usuario está autenticado pero su rol no alcanza.
    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
                       AccessDeniedException error) throws IOException {
        escribir(respuesta, HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
    }

    // Escribe la misma estructura JSON de error que usa el resto de la API (RespuestaError).
    private void escribir(HttpServletResponse respuesta, HttpStatus estado, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(respuesta.getWriter(), new RespuestaError(estado.value(), mensaje, Map.of()));
    }
}
