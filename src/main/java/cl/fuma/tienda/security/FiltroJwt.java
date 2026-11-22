package cl.fuma.tienda.security;

import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Filtro que se ejecuta ANTES de cada petición. Su trabajo:
//   1. Buscar el encabezado "Authorization: Bearer <token>".
//   2. Verificar el token con JwtServicio.
//   3. Si es válido, avisarle a Spring Security quién es el usuario y qué rol tiene.
// Si no hay token (o es inválido), no autentica a nadie y deja seguir la petición:
// después, las reglas de ConfiguracionSeguridad deciden si ese endpoint exigía estar autenticado (401).
public class FiltroJwt extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtServicio jwtServicio;
    private final UsuarioRepository usuarioRepository;

    public FiltroJwt(JwtServicio jwtServicio, UsuarioRepository usuarioRepository) {
        this.jwtServicio = jwtServicio;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {

        String cabecera = peticion.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            String token = cabecera.substring(PREFIJO.length());
            try {
                String correo = jwtServicio.obtenerCorreo(token);

                // El usuario y su rol se leen de la base de datos: si el usuario fue eliminado
                // o cambió de rol, el token viejo deja de dar los permisos anteriores.
                Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);
                if (usuario.isPresent()) {
                    // Spring Security espera los roles con el prefijo "ROLE_" (ROLE_ADMIN, ROLE_CLIENTE...).
                    var autoridad = new SimpleGrantedAuthority("ROLE_" + usuario.get().getRol().name());
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            usuario.get(), null, List.of(autoridad));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException error) {
                // Token alterado, mal formado o vencido: se ignora y la petición sigue sin autenticar.
            }
        }

        cadena.doFilter(peticion, respuesta);
    }
}
