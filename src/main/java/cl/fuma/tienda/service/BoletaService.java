package cl.fuma.tienda.service;

import cl.fuma.tienda.dto.ItemSolicitud;
import cl.fuma.tienda.dto.SolicitudBoleta;
import cl.fuma.tienda.exception.DatosInvalidosException;
import cl.fuma.tienda.exception.PagoRechazadoException;
import cl.fuma.tienda.exception.RecursoNoEncontradoException;
import cl.fuma.tienda.model.Boleta;
import cl.fuma.tienda.model.DetalleBoleta;
import cl.fuma.tienda.model.Producto;
import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.repository.BoletaRepository;
import cl.fuma.tienda.util.Precios;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lógica de la compra: valida los datos, simula el pago, calcula los montos y guarda la boleta.
// Reemplaza lo que hoy hace finalizarCompra() en el frontend, pero en el servidor.
@Service
public class BoletaService {

    // Mismas expresiones que validaciones.js del frontend.
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern TELEFONO = Pattern.compile("^9\\d{8}$"); // celular chileno: 9 dígitos que empiezan con 9
    private static final Pattern TARJETA = Pattern.compile("^\\d{16}$");

    private final BoletaRepository boletaRepository;
    private final ProductoService productoService;

    public BoletaService(BoletaRepository boletaRepository, ProductoService productoService) {
        this.boletaRepository = boletaRepository;
        this.productoService = productoService;
    }

    // ---------- Consultas ----------

    // Todas las boletas (para ADMIN y VENDEDOR).
    public List<Boleta> listar() {
        return boletaRepository.findAllByOrderByFechaDesc();
    }

    // Solo las boletas de un usuario (para que un CLIENTE vea sus compras).
    public List<Boleta> listarDeUsuario(Long idUsuario) {
        return boletaRepository.findByUsuarioIdOrderByFechaDesc(idUsuario);
    }

    public Boleta buscarPorId(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la boleta " + id + "."));
    }

    // ---------- Compra ----------

    // Pasos: 1) validar los datos, 2) simular el pago, 3) armar la boleta con precios del servidor
    // y 4) guardarla. Con @Transactional, si algo falla no queda nada guardado a medias.
    @Transactional
    public Boleta crear(Usuario usuario, SolicitudBoleta solicitud) {
        validar(solicitud);

        // Pago simulado: las tarjetas que empiezan con 0000 se rechazan. NO se crea ninguna boleta.
        // El número de tarjeta solo se usa aquí: no existe ningún campo para guardarlo.
        String tarjeta = solicitud.getNumeroTarjeta().replaceAll("\\s", "");
        if (tarjeta.startsWith("0000")) {
            throw new PagoRechazadoException("El pago fue rechazado. Intenta con otra tarjeta.");
        }

        boolean esDespacho = Precios.ENTREGA_DESPACHO.equals(solicitud.getOpcionEntrega());

        Boleta boleta = new Boleta();
        boleta.setFecha(LocalDateTime.now());
        boleta.setUsuario(usuario);
        boleta.setNombreCliente(solicitud.getNombre().trim());
        boleta.setCorreoCliente(solicitud.getCorreo().trim());
        boleta.setTelefonoCliente(solicitud.getTelefono().replaceAll("\\s", "")); // se guarda sin espacios
        boleta.setOpcionEntrega(solicitud.getOpcionEntrega());
        boleta.setDireccion(esDespacho ? solicitud.getDireccion().trim() : null);

        // Cada línea: se busca el producto en la base y se usa SU precio, no uno enviado por el navegador.
        int subtotal = 0;
        for (ItemSolicitud item : solicitud.getItems()) {
            Producto producto = productoService.buscarPorId(item.getIdProducto());
            int precioUnitario = Precios.calcularPrecioFinal(producto.getPrecio(), producto.getDescuentoPorcentaje());
            boleta.agregarDetalle(new DetalleBoleta(producto, item.getCantidad(), precioUnitario));
            subtotal += precioUnitario * item.getCantidad();
        }

        int costoEnvio = Precios.calcularCostoEnvio(solicitud.getOpcionEntrega());
        boleta.setSubtotal(subtotal);
        boleta.setCostoEnvio(costoEnvio);
        boleta.setTotal(subtotal + costoEnvio);

        // Guarda la boleta y, por el cascade, también sus detalles.
        return boletaRepository.save(boleta);
    }

    // ---------- Método privado de apoyo ----------

    // Reglas del checkout (las mismas de validarDatosCheckout() del frontend, más las de los productos).
    // Junta todos los errores en un mapa (campo -> mensaje) y los lanza juntos.
    private void validar(SolicitudBoleta solicitud) {
        Map<String, String> errores = new HashMap<>();

        if (esVacio(solicitud.getNombre())) {
            errores.put("nombre", "Ingresa tu nombre.");
        }
        if (esVacio(solicitud.getCorreo()) || !CORREO.matcher(solicitud.getCorreo().trim()).matches()) {
            errores.put("correo", "Ingresa un correo válido.");
        }
        if (esVacio(solicitud.getTelefono())
                || !TELEFONO.matcher(solicitud.getTelefono().replaceAll("\\s", "")).matches()) {
            errores.put("telefono", "Ingresa un celular de 9 dígitos que comience con 9.");
        }

        String opcion = solicitud.getOpcionEntrega();
        if (!Precios.ENTREGA_RETIRO.equals(opcion) && !Precios.ENTREGA_DESPACHO.equals(opcion)) {
            errores.put("opcionEntrega", "Elige retiro en tienda o despacho a domicilio.");
        }
        // La dirección solo es obligatoria cuando el pedido se despacha a domicilio.
        if (Precios.ENTREGA_DESPACHO.equals(opcion) && esVacio(solicitud.getDireccion())) {
            errores.put("direccion", "Ingresa una dirección de despacho.");
        }

        if (esVacio(solicitud.getNumeroTarjeta())
                || !TARJETA.matcher(solicitud.getNumeroTarjeta().replaceAll("\\s", "")).matches()) {
            errores.put("numeroTarjeta", "Ingresa una tarjeta de 16 dígitos.");
        }

        if (solicitud.getItems() == null || solicitud.getItems().isEmpty()) {
            errores.put("items", "El carrito está vacío.");
        } else {
            for (ItemSolicitud item : solicitud.getItems()) {
                if (item.getIdProducto() == null || item.getCantidad() < 1) {
                    errores.put("items", "Cada producto debe tener un id y una cantidad de al menos 1.");
                }
            }
        }

        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
