package cl.fuma.tienda.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Datos generales de la documentación Swagger (título y versión).
// springdoc lee los controladores y arma sola la lista de endpoints.
// La página para verla y probar los endpoints queda en: http://localhost:8080/swagger-ui/index.html
@Configuration
public class ConfiguracionSwagger {

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI().info(new Info()
                .title("API de la tienda Fuma")
                .version("v1")
                .description("Servicios REST del backend de Fuma: catálogo, productos y boletas."));
    }
}
