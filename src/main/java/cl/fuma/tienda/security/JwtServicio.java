package cl.fuma.tienda.security;

import cl.fuma.tienda.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Crea y verifica los tokens JWT.
//
// Un JWT es un texto con tres partes separadas por puntos: encabezado . datos . firma
//   - datos: quién es el usuario (su correo), su rol y cuándo vence el token.
//   - firma: el servidor la calcula con su clave secreta. Si alguien cambia los datos
//     (por ejemplo, su rol), la firma ya no coincide y el token se rechaza.
@Service
public class JwtServicio {

    private final SecretKey clave;
    private final long duracionEnMilisegundos;

    // Los valores salen de application.properties (fuma.jwt.secreto y fuma.jwt.expiracion-minutos).
    public JwtServicio(@Value("${fuma.jwt.secreto}") String secreto,
                       @Value("${fuma.jwt.expiracion-minutos}") long minutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionEnMilisegundos = minutos * 60 * 1000;
    }

    // GENERACIÓN: crea un token firmado para el usuario que acaba de iniciar sesión.
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())                 // quién es
                .claim("rol", usuario.getRol().name())        // qué rol tiene
                .issuedAt(ahora)                              // cuándo se creó
                .expiration(new Date(ahora.getTime() + duracionEnMilisegundos)) // cuándo vence
                .signWith(clave, Jwts.SIG.HS256)              // firma con la clave secreta
                .compact();
    }

    // VERIFICACIÓN: comprueba la firma y la fecha de vencimiento, y devuelve los datos del token.
    // Si el token fue alterado, está mal formado o ya venció, lanza una JwtException.
    public Claims leerToken(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Atajo: devuelve el correo (subject) guardado dentro del token.
    public String obtenerCorreo(String token) {
        return leerToken(token).getSubject();
    }
}
