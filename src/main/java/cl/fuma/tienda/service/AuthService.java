package cl.fuma.tienda.service;

import cl.fuma.tienda.dto.SolicitudLogin;
import cl.fuma.tienda.exception.CredencialesInvalidasException;
import cl.fuma.tienda.exception.DatosInvalidosException;
import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.repository.UsuarioRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Lógica del inicio de sesión: comprueba el correo y la contraseña.
// El token JWT lo crea JwtServicio; este servicio solo decide si las credenciales son correctas.
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Devuelve el usuario si el correo existe y la contraseña coincide con el hash guardado.
    public Usuario iniciarSesion(SolicitudLogin solicitud) {
        Map<String, String> errores = new HashMap<>();
        if (solicitud.getCorreo() == null || solicitud.getCorreo().isBlank()) {
            errores.put("correo", "Ingresa tu correo.");
        }
        if (solicitud.getContrasena() == null || solicitud.getContrasena().isBlank()) {
            errores.put("contrasena", "Ingresa tu contraseña.");
        }
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }

        // matches() cifra la contraseña escrita y la compara con el hash de la base de datos.
        // Se usa el MISMO mensaje si falla el correo o la contraseña, para no revelar qué correos existen.
        return usuarioRepository.findByCorreo(solicitud.getCorreo().trim())
                .filter(usuario -> passwordEncoder.matches(solicitud.getContrasena(), usuario.getContrasena()))
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos."));
    }
}
