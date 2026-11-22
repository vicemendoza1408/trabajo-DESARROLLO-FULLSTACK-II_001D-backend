package cl.fuma.tienda.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Datos generales de la documentación Swagger (título y versión).
// springdoc lee los controladores y arma sola la lista de endpoints.
// La página para verla y probar los endpoints queda en: http://localhost:8080/swagger-ui/index.html
//
// Para probar los endpoints protegidos: 1) ejecutar POST /api/v1/auth/login, 2) copiar el "token" de la respuesta,
// 3) botón "Authorize" (arriba a la derecha) y pegar el token. Los endpoints con candado usan ese token.
@Configuration
public class ConfiguracionSwagger {

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de la tienda Fuma")
                        .version("v1")
                        .description("Servicios REST del backend de Fuma: catálogo, productos, boletas y autenticación con JWT."))
                // Define el esquema "bearerAuth": un token JWT enviado en el encabezado Authorization.
                // Los controladores lo piden con @SecurityRequirement(name = "bearerAuth").
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
