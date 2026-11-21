package cl.fuma.tienda.service;

import cl.fuma.tienda.dto.SolicitudProducto;
import cl.fuma.tienda.exception.DatosInvalidosException;
import cl.fuma.tienda.exception.RecursoNoEncontradoException;
import cl.fuma.tienda.model.Categoria;
import cl.fuma.tienda.model.Producto;
import cl.fuma.tienda.repository.DetalleBoletaRepository;
import cl.fuma.tienda.repository.ProductoRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lógica de los productos: consultar el catálogo y administrarlo (crear, editar y eliminar).
// Las reglas de validación son las mismas de validarProducto() del frontend.
@Service
public class ProductoService {

    // Imagen que se usa cuando el producto no trae una (el sistema no carga imágenes).
    private static final String IMAGEN_POR_DEFECTO = "/img/placeholder.svg";

    private final ProductoRepository productoRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository,
                           DetalleBoletaRepository detalleBoletaRepository,
                           CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.detalleBoletaRepository = detalleBoletaRepository;
        this.categoriaService = categoriaService;
    }

    // ---------- Consultas ----------

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public List<Producto> listarPorCategoria(String idCategoria) {
        return productoRepository.findByCategoriaId(idCategoria);
    }

    // Ofertas = productos con algún descuento (mayor que 0 %).
    public List<Producto> listarOfertas() {
        return productoRepository.findByDescuentoPorcentajeGreaterThan(0);
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto " + id + "."));
    }

    // ---------- Administración (solo ADMIN, se controla en la Fase 4) ----------

    @Transactional
    public Producto crear(SolicitudProducto solicitud) {
        validar(solicitud);
        Producto producto = new Producto();
        copiarDatos(solicitud, producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, SolicitudProducto solicitud) {
        Producto producto = buscarPorId(id);
        validar(solicitud);
        copiarDatos(solicitud, producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        // Si el producto ya se vendió, borrarlo dejaría boletas apuntando a algo que no existe.
        // Por eso se bloquea con un mensaje claro y se conserva el historial de ventas.
        if (detalleBoletaRepository.existsByProductoId(id)) {
            throw new DatosInvalidosException(
                    "No se puede eliminar el producto porque ya tiene ventas registradas.");
        }
        productoRepository.delete(producto);
    }

    // ---------- Métodos privados de apoyo ----------

    // Reglas: nombre obligatorio, precio entero mayor que 0, descuento entre 0 y 100 y categoría existente.
    // Junta todos los errores en un mapa (campo -> mensaje) y los lanza juntos, igual que el frontend.
    private void validar(SolicitudProducto solicitud) {
        Map<String, String> errores = new HashMap<>();

        if (solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            errores.put("nombre", "Ingresa el nombre del producto.");
        }
        if (solicitud.getPrecio() <= 0) {
            errores.put("precio", "El precio debe ser un número entero mayor que 0.");
        }
        if (solicitud.getDescuentoPorcentaje() < 0 || solicitud.getDescuentoPorcentaje() > 100) {
            errores.put("descuentoPorcentaje", "El descuento debe ser un número entero entre 0 y 100.");
        }
        if (solicitud.getIdCategoria() == null || !categoriaService.existe(solicitud.getIdCategoria())) {
            errores.put("idCategoria", "Selecciona una categoría.");
        }

        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
    }

    // Pasa los datos de la solicitud al producto. Antes se llamó a validar(),
    // así que aquí la categoría ya se sabe que existe.
    private void copiarDatos(SolicitudProducto solicitud, Producto producto) {
        Categoria categoria = categoriaService.buscarPorId(solicitud.getIdCategoria());
        producto.setNombre(solicitud.getNombre().trim());
        producto.setDescripcion(solicitud.getDescripcion());
        producto.setPrecio(solicitud.getPrecio());
        producto.setDescuentoPorcentaje(solicitud.getDescuentoPorcentaje());
        producto.setImagen(solicitud.getImagen() == null || solicitud.getImagen().isBlank()
                ? IMAGEN_POR_DEFECTO
                : solicitud.getImagen());
        producto.setCategoria(categoria);
    }
}
