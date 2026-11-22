package cl.fuma.tienda.dto;

// Respuesta del login y de la renovación: el token JWT y los datos del usuario.
// El frontend guarda el token y lo envía en cada petición protegida:
//     Authorization: Bearer <token>
public record RespuestaAuth(String token, UsuarioRespuesta usuario) {
}
