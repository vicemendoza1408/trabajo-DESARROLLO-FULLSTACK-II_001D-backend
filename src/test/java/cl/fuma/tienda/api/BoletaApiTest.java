package cl.fuma.tienda.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

// Pruebas de integración de las boletas: la compra completa y los permisos de lectura por rol.
class BoletaApiTest extends BaseApiTest {

    private static final String TARJETA_VALIDA = "4111111111111111";

    // Hace una compra con el token indicado y devuelve el id de la boleta creada.
    private int comprar(String token, int idProducto, int cantidad, String entrega) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(idProducto, cantidad, entrega, TARJETA_VALIDA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return leerEntero(respuesta, "$.id");
    }

    // ---------- La compra ----------

    @Test
    void unClienteCompraConDespachoYElServidorCalculaLosMontos() throws Exception {
        // Producto 2: Polerón gris, 24990 con 20 % de descuento -> 19992 c/u. Dos unidades + envío 3990.
        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(tokenCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(2, 2, "despacho", TARJETA_VALIDA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].precioUnitario").value(19992))
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.subtotal").value(39984))
                .andExpect(jsonPath("$.costoEnvio").value(3990))
                .andExpect(jsonPath("$.total").value(43974))
                .andExpect(jsonPath("$.opcionEntrega").value("despacho"));
    }

    @Test
    void conRetiroEnTiendaNoSeCobraEnvio() throws Exception {
        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(tokenCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(2, 2, "retiro", TARJETA_VALIDA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costoEnvio").value(0))
                .andExpect(jsonPath("$.total").value(39984));
    }

    @Test
    void unaTarjetaRechazadaDa402() throws Exception {
        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(tokenCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(1, 1, "retiro", "0000111122223333")))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.estado").value(402));
    }

    @Test
    void unaCompraSinProductosDa400() throws Exception {
        String sinItems = """
                {"nombre":"Cliente Demo","correo":"cliente@fuma.cl","telefono":"912345678",
                 "opcionEntrega":"retiro","numeroTarjeta":"4111111111111111","items":[]}
                """;

        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(tokenCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sinItems))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.items").exists());
    }

    @Test
    void unaCompraConUnProductoQueNoExisteDa404() throws Exception {
        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(tokenCliente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(9999, 1, "retiro", TARJETA_VALIDA)))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinIniciarSesionNoSePuedeComprarYDa401() throws Exception {
        mockMvc.perform(post("/api/v1/boletas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(1, 1, "retiro", TARJETA_VALIDA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unVendedorNoPuedeComprarYDa403() throws Exception {
        mockMvc.perform(post("/api/v1/boletas")
                        .header("Authorization", bearer(tokenVendedor()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson(1, 1, "retiro", TARJETA_VALIDA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdminTambienPuedeComprar() throws Exception {
        comprar(tokenAdmin(), 1, 1, "retiro");
    }

    // ---------- Lista de boletas ----------

    @Test
    void sinIniciarSesionNoSePuedeVerLaListaYDa401() throws Exception {
        mockMvc.perform(get("/api/v1/boletas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unClienteNoPuedeVerLaListaDeBoletasYDa403() throws Exception {
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", bearer(tokenCliente())))
                .andExpect(status().isForbidden());
    }

    @Test
    void elVendedorYElAdminPuedenVerLaListaDeBoletas() throws Exception {
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", bearer(tokenVendedor())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/boletas").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isOk());
    }

    @Test
    void laListaMuestraPrimeroLaBoletaMasNueva() throws Exception {
        int idNueva = comprar(tokenCliente(), 3, 1, "retiro");

        mockMvc.perform(get("/api/v1/boletas").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idNueva));
    }

    // ---------- Detalle de una boleta ----------

    @Test
    void unClienteVeSuPropiaBoletaPeroNoLaDeOtro() throws Exception {
        int boletaDelAdmin = comprar(tokenAdmin(), 1, 1, "retiro");
        int boletaDelCliente = comprar(tokenCliente(), 1, 1, "retiro");
        String cliente = bearer(tokenCliente());

        mockMvc.perform(get("/api/v1/boletas/" + boletaDelCliente).header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boletaDelCliente));
        mockMvc.perform(get("/api/v1/boletas/" + boletaDelAdmin).header("Authorization", cliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void elVendedorYElAdminVenElDetalleDeCualquierBoleta() throws Exception {
        int boletaDelCliente = comprar(tokenCliente(), 1, 1, "retiro");

        mockMvc.perform(get("/api/v1/boletas/" + boletaDelCliente).header("Authorization", bearer(tokenVendedor())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/boletas/" + boletaDelCliente).header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isOk());
    }

    @Test
    void sinIniciarSesionNoSePuedeVerUnaBoletaYDa401() throws Exception {
        mockMvc.perform(get("/api/v1/boletas/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unaBoletaQueNoExisteDa404() throws Exception {
        mockMvc.perform(get("/api/v1/boletas/999999").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isNotFound());
    }
}
