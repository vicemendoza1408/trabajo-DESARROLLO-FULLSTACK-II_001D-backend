package cl.fuma.tienda.controller;

import cl.fuma.tienda.dto.BoletaRespuesta;
import cl.fuma.tienda.dto.SolicitudBoleta;
import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Endpoints de boletas (las órdenes de compra). Todos exigen iniciar sesión:
//   - Comprar (POST): cliente y administrador.
//   - Listar (GET): administrador y vendedor.
//   - Ver el detalle (GET /{id}): administrador y vendedor ven cualquiera; el cliente solo las suyas.
// Los roles se definen en ConfiguracionSeguridad; la regla "el cliente solo ve las suyas" está en BoletaService.
@RestController
@RequestMapping("/api/v1/boletas")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Boletas", description = "Compras y consulta de las órdenes de compra")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    // La compra queda asociada al usuario que inició sesión (el que identificó el token).
    // Los precios los calcula el servidor: el cliente solo envía idProducto y cantidad.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Realiza una compra y genera la boleta (cliente y administrador)")
    @ApiResponse(responseCode = "201", description = "Compra realizada: boleta creada")
    @ApiResponse(responseCode = "400", description = "Datos de compra inválidos")
    @ApiResponse(responseCode = "401", description = "No ha iniciado sesión")
    @ApiResponse(responseCode = "402", description = "Pago rechazado")
    @ApiResponse(responseCode = "403", description = "Su rol no puede comprar")
    public BoletaRespuesta crear(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario,
                                 @RequestBody SolicitudBoleta solicitud) {
        return BoletaRespuesta.desde(boletaService.crear(usuario, solicitud));
    }

    @GetMapping
    @Operation(summary = "Lista todas las boletas, de la más nueva a la más antigua (administrador y vendedor)")
    @ApiResponse(responseCode = "200", description = "Lista de boletas")
    @ApiResponse(responseCode = "401", description = "No ha iniciado sesión")
    @ApiResponse(responseCode = "403", description = "Su rol no puede ver la lista")
    public List<BoletaRespuesta> listar() {
        return boletaService.listar().stream().map(BoletaRespuesta::desde).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de una boleta por su id (el cliente solo puede ver las suyas)")
    @ApiResponse(responseCode = "200", description = "Boleta encontrada")
    @ApiResponse(responseCode = "401", description = "No ha iniciado sesión")
    @ApiResponse(responseCode = "403", description = "Es una boleta de otro usuario")
    @ApiResponse(responseCode = "404", description = "No existe la boleta")
    public BoletaRespuesta buscarPorId(@PathVariable Long id,
                                       @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return BoletaRespuesta.desde(boletaService.buscarPorId(id, usuario));
    }
}
