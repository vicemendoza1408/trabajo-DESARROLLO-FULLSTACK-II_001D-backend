package cl.fuma.tienda.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.fuma.tienda.model.Rol;
import cl.fuma.tienda.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

// Pruebas unitarias de la generación y verificación de tokens JWT. No usan Spring ni base de datos.
class JwtServicioTest {

    private static final String SECRETO = "clave-de-pruebas-con-mas-de-32-caracteres-123456";
    private static final String OTRO_SECRETO = "otra-clave-distinta-con-mas-de-32-caracteres-9876";

    private final JwtServicio servicio = new JwtServicio(SECRETO, 30);
    private final Usuario usuario = new Usuario("Administrador", "admin@fuma.cl", "hash", "987654321", "Local 1", Rol.ADMIN);

    @Test
    void elTokenTieneTresPartesSeparadasPorPuntos() {
        String token = servicio.generarToken(usuario);

        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void unTokenValidoEntregaElCorreoYElRol() {
        String token = servicio.generarToken(usuario);

        Claims datos = servicio.leerToken(token);

        assertEquals("admin@fuma.cl", datos.getSubject());
        assertEquals("ADMIN", datos.get("rol", String.class));
    }

    @Test
    void elTokenVenceEn30Minutos() {
        Claims datos = servicio.leerToken(servicio.generarToken(usuario));

        long duracion = datos.getExpiration().getTime() - datos.getIssuedAt().getTime();

        assertEquals(30 * 60 * 1000, duracion);
    }

    @Test
    void unTokenVencidoSeRechaza() {
        // Un servicio con duración negativa crea tokens que ya nacieron vencidos.
        JwtServicio servicioQueVenceAntes = new JwtServicio(SECRETO, -1);
        String tokenVencido = servicioQueVenceAntes.generarToken(usuario);

        assertThrows(JwtException.class, () -> servicio.leerToken(tokenVencido));
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        String tokenFalsificado = new JwtServicio(OTRO_SECRETO, 30).generarToken(usuario);

        assertThrows(JwtException.class, () -> servicio.leerToken(tokenFalsificado));
    }

    @Test
    void unTokenAlteradoSeRechaza() {
        String[] partes = servicio.generarToken(usuario).split("\\.");
        // Se cambia un carácter de la parte de los datos: la firma ya no coincide.
        char original = partes[1].charAt(10);
        char cambiado = original == 'A' ? 'B' : 'A';
        String datosAlterados = partes[1].substring(0, 10) + cambiado + partes[1].substring(11);
        String tokenAlterado = partes[0] + "." + datosAlterados + "." + partes[2];

        assertThrows(JwtException.class, () -> servicio.leerToken(tokenAlterado));
    }

    @Test
    void unTextoQueNoEsTokenSeRechaza() {
        assertThrows(JwtException.class, () -> servicio.leerToken("esto-no-es-un-token"));
    }
}
