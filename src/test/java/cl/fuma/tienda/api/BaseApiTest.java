package cl.fuma.tienda.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// Base de las pruebas de integración de la API.
//   @SpringBootTest        levanta la aplicación completa (controladores, servicios, seguridad y JWT reales).
//   @AutoConfigureMockMvc  permite hacer peticiones HTTP simuladas sin abrir un puerto.
//   @ActiveProfiles("test") usa application-test.properties: una base H2 en memoria en vez de MySQL.
// Al arrancar se cargan los datos iniciales (8 productos y los 3 usuarios de prueba), igual que en la app real.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseApiTest {

    @Autowired
    protected MockMvc mockMvc;

    // Inicia sesión por la API real y devuelve el token JWT, listo para usarlo en "Authorization".
    protected String token(String correo, String contrasena) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(respuesta, "$.token");
    }

    protected String tokenAdmin() throws Exception {
        return token("admin@fuma.cl", "admin123");
    }

    protected String tokenVendedor() throws Exception {
        return token("vendedor@fuma.cl", "vendedor123");
    }

    protected String tokenCliente() throws Exception {
        return token("cliente@fuma.cl", "cliente123");
    }

    // Valor del encabezado Authorization para un token.
    protected String bearer(String token) {
        return "Bearer " + token;
    }

    // JSON de un producto válido para crear o editar.
    protected String productoJson(String nombre, int precio) {
        return """
                {"nombre":"%s","descripcion":"Producto de prueba","precio":%d,
                 "descuentoPorcentaje":0,"idCategoria":"accesorios"}
                """.formatted(nombre, precio);
    }

    // JSON de una compra de un solo producto. La dirección solo se usa con "despacho".
    protected String compraJson(int idProducto, int cantidad, String opcionEntrega, String tarjeta) {
        return """
                {"nombre":"Cliente Demo","correo":"cliente@fuma.cl","telefono":"912345678",
                 "opcionEntrega":"%s","direccion":"Calle Falsa 123","numeroTarjeta":"%s",
                 "items":[{"idProducto":%d,"cantidad":%d}]}
                """.formatted(opcionEntrega, tarjeta, idProducto, cantidad);
    }

    // Lee un número (por ejemplo el id) de una respuesta JSON.
    protected int leerEntero(String json, String ruta) {
        return JsonPath.read(json, ruta);
    }
}
