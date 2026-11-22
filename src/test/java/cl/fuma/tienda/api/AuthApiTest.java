package cl.fuma.tienda.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.fuma.tienda.model.Rol;
import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.security.JwtServicio;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

// Pruebas de integración de la autenticación: login, /me, renovación y tokens inválidos.
class AuthApiTest extends BaseApiTest {

    @Value("${fuma.jwt.secreto}")
    private String secretoJwt;

    private String loginJson(String correo, String contrasena) {
        return "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}";
    }

    @Test
    void loginCorrectoEntregaTokenYDatosDelUsuarioSinLaContrasena() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin@fuma.cl", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.correo").value("admin@fuma.cl"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
                .andExpect(jsonPath("$.usuario.contrasena").doesNotExist());
    }

    @Test
    void cadaUsuarioDePruebaRecibeSuRol() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("vendedor@fuma.cl", "vendedor123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("VENDEDOR"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("cliente@fuma.cl", "cliente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
    }

    @Test
    void loginConContrasenaIncorrectaDa401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin@fuma.cl", "contrasena-mala")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void loginConUnCorreoQueNoExisteDa401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("nadie@fuma.cl", "admin123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSinDatosDa400ConLosCamposQueFaltan() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists());
    }

    @Test
    void meEntregaElUsuarioDuenoDelToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(tokenCliente())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("cliente@fuma.cl"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void meSinTokenDa401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meConUnTokenFalsoDa401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer token.falso.123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void renovarEntregaUnTokenQueSirveParaSeguirUsandoLaApi() throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", bearer(tokenCliente())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String tokenNuevo = JsonPath.read(respuesta, "$.token");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(tokenNuevo)))
                .andExpect(status().isOk());
    }

    @Test
    void renovarSinTokenDa401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenVencidoDa401() throws Exception {
        // Mismo secreto que el servidor, pero con duración negativa: el token nace vencido.
        Usuario admin = new Usuario("Administrador", "admin@fuma.cl", "hash", "1", "x", Rol.ADMIN);
        String tokenVencido = new JwtServicio(secretoJwt, -1).generarToken(admin);

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(tokenVencido)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenFirmadoConOtraClaveDa401() throws Exception {
        Usuario admin = new Usuario("Administrador", "admin@fuma.cl", "hash", "1", "x", Rol.ADMIN);
        String tokenFalsificado = new JwtServicio("otra-clave-distinta-con-mas-de-32-caracteres-9876", 30)
                .generarToken(admin);

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(tokenFalsificado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenDeUnUsuarioQueNoExisteDa401() throws Exception {
        // Token con firma válida, pero el correo no está en la base de datos.
        Usuario fantasma = new Usuario("Fantasma", "fantasma@fuma.cl", "hash", "1", "x", Rol.ADMIN);
        String token = new JwtServicio(secretoJwt, 30).generarToken(fantasma);

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }
}
