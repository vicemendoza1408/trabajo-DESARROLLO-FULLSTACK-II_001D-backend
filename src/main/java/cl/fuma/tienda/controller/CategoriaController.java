package cl.fuma.tienda.controller;

import cl.fuma.tienda.model.Categoria;
import cl.fuma.tienda.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints de categorías. El controlador solo recibe la petición y llama al servicio.
// /api/v1 es la versión de la API: si algún día cambia el formato, se crea /api/v2 sin romper el frontend.
// Devuelve la entidad Categoria directamente porque solo tiene id, nombre y descripción (nada sensible).
@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "Consulta de las categorías de la tienda")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Lista todas las categorías")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    public List<Categoria> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una categoría por su id (por ejemplo: poleras)")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada")
    @ApiResponse(responseCode = "404", description = "No existe la categoría")
    public Categoria buscarPorId(@PathVariable String id) {
        return categoriaService.buscarPorId(id);
    }
}
