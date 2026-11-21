package cl.fuma.tienda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla USUARIO. Guarda a quienes pueden iniciar sesión: administrador, vendedor y clientes.
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // El correo es el nombre de usuario para iniciar sesión, por eso no puede repetirse.
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    // Aquí se guardará la contraseña CIFRADA (hash BCrypt), nunca en texto plano.
    // Se implementa en la Fase 4 (seguridad). El hash BCrypt mide 60 caracteres.
    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    // Se guarda como texto ("ADMIN", "VENDEDOR" o "CLIENTE") para que sea legible en la tabla.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String contrasena, String telefono,
                   String direccion, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.direccion = direccion;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
