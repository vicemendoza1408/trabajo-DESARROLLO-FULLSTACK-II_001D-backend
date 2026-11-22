package cl.fuma.tienda.dto;

import cl.fuma.tienda.model.Usuario;

// Datos del usuario que el backend entrega al frontend.
// A propósito NO incluye la contraseña (ni siquiera cifrada): lo que no se envía no se puede filtrar.
public record UsuarioRespuesta(
        Long id,
        String nombre,
        String correo,
        String telefono,
        String direccion,
        String rol) {

    // Convierte la entidad Usuario (tabla) en la respuesta que se envía.
    public static UsuarioRespuesta desde(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getRol().name());
    }
}
