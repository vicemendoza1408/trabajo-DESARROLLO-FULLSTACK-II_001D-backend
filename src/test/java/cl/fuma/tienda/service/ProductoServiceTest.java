package cl.fuma.tienda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.fuma.tienda.dto.SolicitudProducto;
import cl.fuma.tienda.exception.DatosInvalidosException;
import cl.fuma.tienda.exception.RecursoNoEncontradoException;
import cl.fuma.tienda.model.Categoria;
import cl.fuma.tienda.model.Producto;
import cl.fuma.tienda.repository.DetalleBoletaRepository;
import cl.fuma.tienda.repository.ProductoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Pruebas unitarias de ProductoService. Los repositorios y el servicio de categorías son
// "mocks" (simulacros de Mockito): no hay base de datos, solo se prueba la lógica del servicio.
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private DetalleBoletaRepository detalleBoletaRepository;
    @Mock
    private CategoriaService categoriaService;

    // Mockito crea el servicio real y le entrega los mocks de arriba.
    @InjectMocks
    private ProductoService productoService;

    private SolicitudProducto solicitudValida() {
        SolicitudProducto solicitud = new SolicitudProducto();
        solicitud.setNombre("  Gorro de lana  ");
        solicitud.setDescripcion("Gorro tejido");
        solicitud.setPrecio(6990);
        solicitud.setDescuentoPorcentaje(10);
        solicitud.setImagen("");
        solicitud.setIdCategoria("accesorios");
        return solicitud;
    }

    @Test
    void buscarUnProductoQueNoExisteLanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.buscarPorId(99L));
    }

    @Test
    void listarOfertasPideLosProductosConDescuentoMayorQueCero() {
        productoService.listarOfertas();

        verify(productoRepository).findByDescuentoPorcentajeGreaterThan(0);
    }

    @Test
    void crearUnProductoValidoLoGuardaSinEspaciosYConImagenPorDefecto() {
        when(categoriaService.existe("accesorios")).thenReturn(true);
        when(categoriaService.buscarPorId("accesorios"))
                .thenReturn(new Categoria("accesorios", "Accesorios", "Gorros y más"));
        when(productoRepository.save(any(Producto.class))).thenAnswer(llamada -> llamada.getArgument(0));

        Producto creado = productoService.crear(solicitudValida());

        assertEquals("Gorro de lana", creado.getNombre());
        assertEquals("/img/placeholder.svg", creado.getImagen());
        assertEquals("accesorios", creado.getCategoria().getId());
    }

    @Test
    void crearConVariosDatosInvalidosJuntaTodosLosErroresYNoGuarda() {
        SolicitudProducto solicitud = new SolicitudProducto();
        solicitud.setNombre("");
        solicitud.setPrecio(0);
        solicitud.setDescuentoPorcentaje(150);
        solicitud.setIdCategoria("no-existe"); // el mock responde false para una categoría desconocida

        DatosInvalidosException error =
                assertThrows(DatosInvalidosException.class, () -> productoService.crear(solicitud));

        assertTrue(error.getErrores().containsKey("nombre"));
        assertTrue(error.getErrores().containsKey("precio"));
        assertTrue(error.getErrores().containsKey("descuentoPorcentaje"));
        assertTrue(error.getErrores().containsKey("idCategoria"));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizarUnProductoQueNoExisteLanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> productoService.actualizar(99L, solicitudValida()));
    }

    @Test
    void noSePuedeEliminarUnProductoQueYaTieneVentas() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto()));
        when(detalleBoletaRepository.existsByProductoId(1L)).thenReturn(true);

        assertThrows(DatosInvalidosException.class, () -> productoService.eliminar(1L));

        verify(productoRepository, never()).delete(any());
    }

    @Test
    void unProductoSinVentasSeElimina() {
        Producto producto = new Producto();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(detalleBoletaRepository.existsByProductoId(1L)).thenReturn(false);

        productoService.eliminar(1L);

        verify(productoRepository).delete(producto);
    }
}
