package cl.fuma.tienda.api;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

// Pruebas de integración de categorías y productos: consulta pública, CRUD del administrador
// y permisos por rol. Se hacen peticiones HTTP reales a la API (con seguridad y base H2).
class ProductoApiTest extends BaseApiTest {

    // ---------- Consulta pública (sin iniciar sesión) ----------

    @Test
    void cualquieraPuedeVerLaListaDeProductos() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(8)));
    }

    @Test
    void sePuedeFiltrarLosProductosPorCategoria() throws Exception {
        mockMvc.perform(get("/api/v1/productos").param("idCategoria", "poleras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].idCategoria", everyItem(is("poleras"))));
    }

    @Test
    void lasOfertasSoloTraenProductosConDescuento() throws Exception {
        mockMvc.perform(get("/api/v1/productos").param("ofertas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].descuentoPorcentaje", everyItem(greaterThan(0))));
    }

    @Test
    void elDetalleDeUnProductoTraeSusDatos() throws Exception {
        mockMvc.perform(get("/api/v1/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.precio").value(29990))
                .andExpect(jsonPath("$.idCategoria").value("polerones"));
    }

    @Test
    void unProductoQueNoExisteDa404() throws Exception {
        mockMvc.perform(get("/api/v1/productos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    void unIdQueNoEsNumeroDa400() throws Exception {
        mockMvc.perform(get("/api/v1/productos/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cualquieraPuedeVerLasCategorias() throws Exception {
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        mockMvc.perform(get("/api/v1/categorias/poleras"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/categorias/no-existe"))
                .andExpect(status().isNotFound());
    }

    // ---------- Administración de productos (solo ADMIN) ----------

    @Test
    void elAdminCreaEditaYEliminaUnProducto() throws Exception {
        String admin = bearer(tokenAdmin());

        // Crear
        String creado = mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("Calcetines de prueba", 4990)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Calcetines de prueba"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        int id = leerEntero(creado, "$.id");

        // Editar
        mockMvc.perform(put("/api/v1/productos/" + id)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("Calcetines editados", 5990)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Calcetines editados"))
                .andExpect(jsonPath("$.precio").value(5990));

        // Eliminar
        mockMvc.perform(delete("/api/v1/productos/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/productos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearUnProductoConDatosInvalidosDa400ConLosErrores() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("", -5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.precio").exists());
    }

    @Test
    void editarUnProductoQueNoExisteDa404() throws Exception {
        mockMvc.perform(put("/api/v1/productos/9999")
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("Nada", 1000)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unProductoConVentasNoSePuedeEliminar() throws Exception {
        // El cliente compra el producto 8 y después el admin intenta eliminarlo.
        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(tokenCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(8, 1, "retiro", "4111111111111111")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/productos/8").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/productos/8"))
                .andExpect(status().isOk());
    }

    // ---------- Permisos: quién NO puede administrar productos ----------

    @Test
    void sinIniciarSesionNoSePuedeCrearUnProductoYDa401() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("Intruso", 1000)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unClienteNoPuedeCrearEditarNiEliminarProductosYDa403() throws Exception {
        String cliente = bearer(tokenCliente());

        mockMvc.perform(post("/api/v1/productos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(productoJson("Intruso", 1000)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/productos/1").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(productoJson("Intruso", 1000)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/productos/1").header("Authorization", cliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void unVendedorNoPuedeCrearEditarNiEliminarProductosYDa403() throws Exception {
        String vendedor = bearer(tokenVendedor());

        mockMvc.perform(post("/api/v1/productos").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(productoJson("Intruso", 1000)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/productos/1").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(productoJson("Intruso", 1000)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/productos/1").header("Authorization", vendedor))
                .andExpect(status().isForbidden());
    }

    @Test
    void unVendedorSiPuedeVerLosProductos() throws Exception {
        mockMvc.perform(get("/api/v1/productos").header("Authorization", bearer(tokenVendedor())))
                .andExpect(status().isOk());
    }
}
