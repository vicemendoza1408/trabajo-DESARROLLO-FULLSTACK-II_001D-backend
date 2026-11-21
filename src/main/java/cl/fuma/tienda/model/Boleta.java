package cl.fuma.tienda.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Tabla BOLETA. Es el "pedido" del frontend: una compra aprobada de un usuario.
// Sus productos están en la tabla DETALLE_BOLETA.
@Entity
@Table(name = "boleta")
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // Muchas boletas -> un usuario (quien compró).
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Datos de contacto escritos en el checkout. Se copian a la boleta porque
    // el cliente pudo cambiarlos respecto de los de su cuenta.
    @Column(nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false, length = 100)
    private String correoCliente;

    @Column(nullable = false, length = 20)
    private String telefonoCliente;

    // "retiro" o "despacho", los mismos valores que usa el frontend.
    @Column(nullable = false, length = 20)
    private String opcionEntrega;

    // Solo tiene valor cuando la entrega es "despacho". Con "retiro" queda vacío (null).
    @Column(length = 200)
    private String direccion;

    // Montos en pesos chilenos, sin decimales.
    @Column(nullable = false)
    private int subtotal;

    @Column(nullable = false)
    private int costoEnvio;

    @Column(nullable = false)
    private int total;

    // Una boleta -> muchos detalles. "mappedBy" indica que la clave está en DetalleBoleta.
    // CascadeType.ALL: al guardar la boleta se guardan también sus detalles.
    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    public Boleta() {
    }

    // Agrega un detalle y lo enlaza con esta boleta.
    public void agregarDetalle(DetalleBoleta detalle) {
        detalle.setBoleta(this);
        detalles.add(detalle);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getOpcionEntrega() {
        return opcionEntrega;
    }

    public void setOpcionEntrega(String opcionEntrega) {
        this.opcionEntrega = opcionEntrega;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    public int getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(int costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<DetalleBoleta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleBoleta> detalles) {
        this.detalles = detalles;
    }
}
