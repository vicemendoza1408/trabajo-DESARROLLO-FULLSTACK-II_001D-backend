package cl.fuma.tienda.controller;

import cl.fuma.tienda.dto.BoletaRespuesta;
import cl.fuma.tienda.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints de boletas (las órdenes de compra).
// Comprar (POST) se agrega en la Fase 4, porque necesita saber qué usuario inició sesión.
@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Consulta de las órdenes de compra")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @GetMapping
    @Operation(summary = "Lista todas las boletas, de la más nueva a la más antigua (administrador y vendedor)")
    @ApiResponse(responseCode = "200", description = "Lista de boletas")
    public List<BoletaRespuesta> listar() {
        return boletaService.listar().stream().map(BoletaRespuesta::desde).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de una boleta por su id")
    @ApiResponse(responseCode = "200", description = "Boleta encontrada")
    @ApiResponse(responseCode = "404", description = "No existe la boleta")
    public BoletaRespuesta buscarPorId(@PathVariable Long id) {
        return BoletaRespuesta.desde(boletaService.buscarPorId(id));
    }
}
