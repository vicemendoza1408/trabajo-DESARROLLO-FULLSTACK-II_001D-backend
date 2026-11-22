package cl.fuma.tienda.controller;

import cl.fuma.tienda.dto.RespuestaAuth;
import cl.fuma.tienda.dto.SolicitudLogin;
import cl.fuma.tienda.dto.UsuarioRespuesta;
import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.security.JwtServicio;
import cl.fuma.tienda.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints de autenticación: iniciar sesión, renovar el token y saber quién soy.
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión con JWT")
public class AuthController {

    private final AuthService authService;
    private final JwtServicio jwtServicio;

    public AuthController(AuthService authService, JwtServicio jwtServicio) {
        this.authService = authService;
        this.jwtServicio = jwtServicio;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión con correo y contraseña y entrega un token JWT")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada: token y datos del usuario")
    @ApiResponse(responseCode = "400", description = "Faltan el correo o la contraseña")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    public RespuestaAuth login(@RequestBody SolicitudLogin solicitud) {
        Usuario usuario = authService.iniciarSesion(solicitud);
        return new RespuestaAuth(jwtServicio.generarToken(usuario), UsuarioRespuesta.desde(usuario));
    }

    // Renovación: con un token todavía válido se obtiene uno nuevo con la duración completa.
    // Así la sesión no se corta mientras la persona sigue usando la tienda.
    // @AuthenticationPrincipal entrega el usuario que el FiltroJwt identificó con el token.
    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Renueva el token JWT (requiere un token que aún no haya vencido)")
    @ApiResponse(responseCode = "200", description = "Token nuevo y datos del usuario")
    @ApiResponse(responseCode = "401", description = "Token inválido o vencido")
    public RespuestaAuth renovar(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return new RespuestaAuth(jwtServicio.generarToken(usuario), UsuarioRespuesta.desde(usuario));
    }

    // Permite al frontend recuperar la sesión al recargar la página: envía el token guardado
    // y el backend responde quién es y qué rol tiene.
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Devuelve los datos del usuario dueño del token")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @ApiResponse(responseCode = "401", description = "Token inválido o vencido")
    public UsuarioRespuesta yo(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return UsuarioRespuesta.desde(usuario);
    }
}
