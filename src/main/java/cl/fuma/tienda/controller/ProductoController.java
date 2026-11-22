package cl.fuma.tienda.controller;

import cl.fuma.tienda.dto.ProductoRespuesta;
import cl.fuma.tienda.dto.SolicitudProducto;
import cl.fuma.tienda.model.Producto;
import cl.fuma.tienda.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Endpoints de productos: consulta pública y administración (crear, editar y eliminar).
// Cada método llama al servicio y convierte el resultado en ProductoRespuesta (lo que ve el frontend).
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de productos y su administración")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/v1/productos                      -> todos
    // GET /api/v1/productos?idCategoria=poleras  -> solo los de esa categoría
    // GET /api/v1/productos?ofertas=true         -> solo los que tienen descuento
    // La búsqueda por texto la sigue haciendo el frontend sobre esta lista.
    @GetMapping
    @Operation(summary = "Lista los productos, con filtro opcional por categoría u ofertas")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    public List<ProductoRespuesta> listar(
            @RequestParam(required = false) String idCategoria,
            @RequestParam(defaultValue = "false") boolean ofertas) {

        List<Producto> productos;
        if (idCategoria != null) {
            productos = productoService.listarPorCategoria(idCategoria);
        } else if (ofertas) {
            productos = productoService.listarOfertas();
        } else {
            productos = productoService.listar();
        }
        return productos.stream().map(ProductoRespuesta::desde).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un producto por su id")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "No existe el producto")
    public ProductoRespuesta buscarPorId(@PathVariable Long id) {
        return ProductoRespuesta.desde(productoService.buscarPorId(id));
    }

    // @RequestBody toma el JSON que envía el frontend y lo convierte en un SolicitudProducto.
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un producto (solo administrador)")
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "No ha iniciado sesión")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede hacerlo")
    public ProductoRespuesta crear(@RequestBody SolicitudProducto solicitud) {
        return ProductoRespuesta.desde(productoService.crear(solicitud));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualiza un producto existente (solo administrador)")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "No ha iniciado sesión")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede hacerlo")
    @ApiResponse(responseCode = "404", description = "No existe el producto")
    public ProductoRespuesta actualizar(@PathVariable Long id, @RequestBody SolicitudProducto solicitud) {
        return ProductoRespuesta.desde(productoService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un producto (solo administrador). No se puede si ya tiene ventas")
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    @ApiResponse(responseCode = "401", description = "No ha iniciado sesión")
    @ApiResponse(responseCode = "403", description = "Solo el administrador puede hacerlo")
    @ApiResponse(responseCode = "400", description = "El producto ya tiene ventas registradas")
    @ApiResponse(responseCode = "404", description = "No existe el producto")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
