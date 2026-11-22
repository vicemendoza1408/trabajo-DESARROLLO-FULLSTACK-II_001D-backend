package cl.fuma.tienda.config;

import cl.fuma.tienda.repository.UsuarioRepository;
import cl.fuma.tienda.security.FiltroJwt;
import cl.fuma.tienda.security.JwtServicio;
import cl.fuma.tienda.security.ManejadorSeguridad;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Aquí se define QUIÉN puede llamar a CADA endpoint. Es el único lugar donde están las reglas por rol.
//
//   Público (sin iniciar sesión): ver categorías y productos, iniciar sesión y la documentación Swagger.
//   ADMIN: todo (además crea, edita y elimina productos).
//   VENDEDOR: solo ver la lista de productos y de boletas, y el detalle.
//   CLIENTE: la tienda: ver productos y comprar (y ver sus propias boletas).
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final JwtServicio jwtServicio;
    private final UsuarioRepository usuarioRepository;
    private final ManejadorSeguridad manejadorSeguridad;

    public ConfiguracionSeguridad(JwtServicio jwtServicio, UsuarioRepository usuarioRepository,
                                  ManejadorSeguridad manejadorSeguridad) {
        this.jwtServicio = jwtServicio;
        this.usuarioRepository = usuarioRepository;
        this.manejadorSeguridad = manejadorSeguridad;
    }

    // Cifrador de contraseñas. BCrypt guarda un "hash": no se puede volver a la contraseña original,
    // solo comprobar si una contraseña escrita coincide con el hash guardado.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
                // CSRF protege a las aplicaciones que usan cookies de sesión. Esta API usa un token
                // en el encabezado Authorization (no cookies), así que no corresponde y se desactiva.
                .csrf(csrf -> csrf.disable())

                // Usa la configuración de CORS que ya existe (ConfiguracionCors) para el frontend.
                .cors(Customizer.withDefaults())

                // STATELESS: el servidor no guarda sesiones. Cada petición trae su token y se verifica sola.
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Las reglas se leen de arriba hacia abajo y se aplica la primera que coincide.
                .authorizeHttpRequests(reglas -> reglas
                        // Las consultas previas de CORS que hace el navegador (OPTIONS) no llevan token.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Documentación Swagger.
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Iniciar sesión es público. Renovar el token y /me exigen estar autenticado (regla final).
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()

                        // La tienda se puede mirar sin iniciar sesión.
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**", "/api/v1/productos/**").permitAll()

                        // Crear, editar y eliminar productos: solo el administrador.
                        .requestMatchers("/api/v1/productos/**").hasRole("ADMIN")

                        // Lista de boletas: administrador y vendedor.
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas").hasAnyRole("ADMIN", "VENDEDOR")

                        // Comprar: cliente y administrador (el vendedor no compra).
                        .requestMatchers(HttpMethod.POST, "/api/v1/boletas").hasAnyRole("ADMIN", "CLIENTE")

                        // Detalle de una boleta: cualquier usuario con sesión. El servicio comprueba
                        // que un cliente solo pueda ver SUS boletas.
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas/**").authenticated()

                        // Cualquier otra ruta no listada arriba exige iniciar sesión.
                        .anyRequest().authenticated())

                // Qué responder cuando se bloquea una petición: 401 (sin sesión) o 403 (sin permiso).
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(manejadorSeguridad)
                        .accessDeniedHandler(manejadorSeguridad))

                // Nuestro filtro JWT corre antes del filtro de login por formulario de Spring.
                .addFilterBefore(new FiltroJwt(jwtServicio, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
