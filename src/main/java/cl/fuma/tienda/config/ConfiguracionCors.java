package cl.fuma.tienda.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS: por seguridad, el navegador bloquea que una página de un origen (el frontend en localhost:5173)
// llame a otro origen distinto (el backend en localhost:8080), salvo que el backend lo permita.
// Esta configuración le dice al navegador: "las páginas de estos orígenes pueden llamar a /api/**".
@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 5173: servidor de desarrollo de Vite (npm run dev). 4173: vista previa (npm run preview).
                .allowedOrigins("http://localhost:5173", "http://localhost:4173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
