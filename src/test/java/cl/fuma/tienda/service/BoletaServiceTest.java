package cl.fuma.tienda.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.fuma.tienda.dto.ItemSolicitud;
import cl.fuma.tienda.dto.SolicitudBoleta;
import cl.fuma.tienda.exception.AccesoDenegadoException;
import cl.fuma.tienda.exception.DatosInvalidosException;
import cl.fuma.tienda.exception.PagoRechazadoException;
import cl.fuma.tienda.exception.RecursoNoEncontradoException;
import cl.fuma.tienda.model.Boleta;
import cl.fuma.tienda.model.Categoria;
import cl.fuma.tienda.model.Producto;
import cl.fuma.tienda.model.Rol;
import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.repository.BoletaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Pruebas unitarias de BoletaService (la compra). Sin base de datos: el repositorio y el
// servicio de productos son mocks de Mockito.
@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;
    @Mock
    private ProductoService productoService;

    @InjectMocks
    private BoletaService boletaService;

    // Polerón gris básico: 24990 con 20 % de descuento -> precio final 19992.
    private Producto poleronGris() {
        Producto producto = new Producto("Polerón gris básico", "Polerón liso", 24990, 20, "/img/x.jpg",
                new Categoria("polerones", "Polerones", ""));
        producto.setId(1L);
        return producto;
    }

    private Usuario usuario(Long id, Rol rol) {
        Usuario usuario = new Usuario("Usuario " + id, "u" + id + "@fuma.cl", "hash", "912345678", "Calle 1", rol);
        usuario.setId(id);
        return usuario;
    }

    private SolicitudBoleta solicitudValida(String opcionEntrega) {
        SolicitudBoleta solicitud = new SolicitudBoleta();
        solicitud.setNombre("Ana Pérez");
        solicitud.setCorreo("ana@correo.cl");
        solicitud.setTelefono("912345678");
        solicitud.setOpcionEntrega(opcionEntrega);
        solicitud.setDireccion("Calle Falsa 123");
        solicitud.setNumeroTarjeta("4111111111111111");
        ItemSolicitud item = new ItemSolicitud();
        item.setIdProducto(1L);
        item.setCantidad(2);
        solicitud.setItems(List.of(item));
        return solicitud;
    }

    private Boleta boletaDe(Usuario dueno) {
        Boleta boleta = new Boleta();
        boleta.setId(10L);
        boleta.setUsuario(dueno);
        return boleta;
    }

    // ---------- Compra ----------

    @Test
    void conRetiroElTotalEsElSubtotalSinEnvio() {
        when(productoService.buscarPorId(1L)).thenReturn(poleronGris());
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(llamada -> llamada.getArgument(0));
        Usuario comprador = usuario(1L, Rol.CLIENTE);

        Boleta boleta = boletaService.crear(comprador, solicitudValida("retiro"));

        // El precio sale de la base de datos: 19992 por unidad x 2 unidades.
        assertEquals(1, boleta.getDetalles().size());
        assertEquals(19992, boleta.getDetalles().get(0).getPrecioUnitario());
        assertEquals(39984, boleta.getSubtotal());
        assertEquals(0, boleta.getCostoEnvio());
        assertEquals(39984, boleta.getTotal());
        assertSame(comprador, boleta.getUsuario());
    }

    @Test
    void conDespachoSeSumaElEnvioYSeGuardaLaDireccion() {
        when(productoService.buscarPorId(1L)).thenReturn(poleronGris());
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(llamada -> llamada.getArgument(0));

        Boleta boleta = boletaService.crear(usuario(1L, Rol.CLIENTE), solicitudValida("despacho"));

        assertEquals(3990, boleta.getCostoEnvio());
        assertEquals(43974, boleta.getTotal());
        assertEquals("Calle Falsa 123", boleta.getDireccion());
    }

    @Test
    void conRetiroNoSeGuardaLaDireccion() {
        when(productoService.buscarPorId(1L)).thenReturn(poleronGris());
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(llamada -> llamada.getArgument(0));

        Boleta boleta = boletaService.crear(usuario(1L, Rol.CLIENTE), solicitudValida("retiro"));

        assertNull(boleta.getDireccion());
    }

    @Test
    void unaTarjetaQueEmpiezaCon0000SeRechazaYNoSeGuardaNada() {
        SolicitudBoleta solicitud = solicitudValida("retiro");
        solicitud.setNumeroTarjeta("0000 1111 2222 3333");

        assertThrows(PagoRechazadoException.class,
                () -> boletaService.crear(usuario(1L, Rol.CLIENTE), solicitud));

        verify(boletaRepository, never()).save(any());
    }

    @Test
    void conDatosInvalidosSeJuntanTodosLosErroresYNoSeGuardaNada() {
        SolicitudBoleta solicitud = new SolicitudBoleta();
        solicitud.setNombre("");
        solicitud.setCorreo("correo-malo");
        solicitud.setTelefono("123");
        solicitud.setOpcionEntrega("otra");
        solicitud.setNumeroTarjeta("12");
        // items queda vacío (carrito vacío)

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> boletaService.crear(usuario(1L, Rol.CLIENTE), solicitud));

        List<String> camposEsperados = List.of("nombre", "correo", "telefono", "opcionEntrega", "numeroTarjeta", "items");
        for (String campo : camposEsperados) {
            assertTrue(error.getErrores().containsKey(campo), "Debería haber un error en: " + campo);
        }
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void conDespachoLaDireccionEsObligatoria() {
        SolicitudBoleta solicitud = solicitudValida("despacho");
        solicitud.setDireccion("  ");

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> boletaService.crear(usuario(1L, Rol.CLIENTE), solicitud));

        assertTrue(error.getErrores().containsKey("direccion"));
    }

    // ---------- Consulta de boletas ----------

    @Test
    void unaBoletaQueNoExisteLanzaRecursoNoEncontrado() {
        when(boletaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> boletaService.buscarPorId(99L));
    }

    @Test
    void unClientePuedeVerSuPropiaBoleta() {
        Usuario cliente = usuario(1L, Rol.CLIENTE);
        Boleta boleta = boletaDe(cliente);
        when(boletaRepository.findById(10L)).thenReturn(Optional.of(boleta));

        assertSame(boleta, boletaService.buscarPorId(10L, cliente));
    }

    @Test
    void unClienteNoPuedeVerLaBoletaDeOtroUsuario() {
        Boleta boletaDeOtro = boletaDe(usuario(2L, Rol.CLIENTE));
        when(boletaRepository.findById(10L)).thenReturn(Optional.of(boletaDeOtro));

        assertThrows(AccesoDenegadoException.class,
                () -> boletaService.buscarPorId(10L, usuario(1L, Rol.CLIENTE)));
    }

    @Test
    void elVendedorYElAdminPuedenVerLaBoletaDeCualquiera() {
        Boleta boletaDeOtro = boletaDe(usuario(2L, Rol.CLIENTE));
        when(boletaRepository.findById(10L)).thenReturn(Optional.of(boletaDeOtro));

        assertSame(boletaDeOtro, boletaService.buscarPorId(10L, usuario(3L, Rol.VENDEDOR)));
        assertSame(boletaDeOtro, boletaService.buscarPorId(10L, usuario(4L, Rol.ADMIN)));
    }
}
