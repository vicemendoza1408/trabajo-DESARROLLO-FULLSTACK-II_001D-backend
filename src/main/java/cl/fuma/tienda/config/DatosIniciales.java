package cl.fuma.tienda.config;

import cl.fuma.tienda.model.Categoria;
import cl.fuma.tienda.model.Producto;
import cl.fuma.tienda.model.Rol;
import cl.fuma.tienda.model.Usuario;
import cl.fuma.tienda.repository.CategoriaRepository;
import cl.fuma.tienda.repository.ProductoRepository;
import cl.fuma.tienda.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Carga los datos iniciales (catálogo y usuarios de prueba) la primera vez que se levanta el backend.
// CommandLineRunner se ejecuta al arrancar la aplicación.
// El catálogo y los usuarios se cargan por separado: cada uno solo se crea si su tabla está vacía.
@Component
public class DatosIniciales implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring entrega los repositorios y el cifrador solos (inyección de dependencias por constructor).
    public DatosIniciales(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                          UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        cargarCatalogo();
        cargarUsuarios();
    }

    // Usuarios de prueba, uno por rol. Las contraseñas se guardan CIFRADAS con BCrypt (nunca en texto plano).
    //   admin@fuma.cl / admin123       -> ADMIN    (acceso total)
    //   vendedor@fuma.cl / vendedor123 -> VENDEDOR (solo ve productos y boletas)
    //   cliente@fuma.cl / cliente123   -> CLIENTE  (solo la tienda)
    private void cargarUsuarios() {
        // Si ya hay usuarios, no se vuelve a cargar nada.
        if (usuarioRepository.count() > 0) {
            return;
        }

        usuarioRepository.save(new Usuario("Administrador Fuma", "admin@fuma.cl",
                passwordEncoder.encode("admin123"), "987654321", "Local Fuma 1", Rol.ADMIN));
        usuarioRepository.save(new Usuario("Vendedor Fuma", "vendedor@fuma.cl",
                passwordEncoder.encode("vendedor123"), "976543210", "Local Fuma 1", Rol.VENDEDOR));
        usuarioRepository.save(new Usuario("Cliente Demo", "cliente@fuma.cl",
                passwordEncoder.encode("cliente123"), "912345678", "Av. Principal 123", Rol.CLIENTE));
    }

    // Las mismas categorías y productos que tenía el frontend.
    private void cargarCatalogo() {
        // Si ya hay categorías, el catálogo ya está cargado.
        if (categoriaRepository.count() > 0) {
            return;
        }

        Categoria polerones = categoriaRepository.save(
                new Categoria("polerones", "Polerones", "Abrigo cómodo para el día a día."));
        Categoria poleras = categoriaRepository.save(
                new Categoria("poleras", "Poleras", "Básicos y estampados de algodón."));
        Categoria pantalones = categoriaRepository.save(
                new Categoria("pantalones", "Pantalones", "Jeans, cargo y jogger."));
        Categoria accesorios = categoriaRepository.save(
                new Categoria("accesorios", "Accesorios", "Gorros, cinturones y más."));

        // Orden de los datos: nombre, descripción, precio, % de descuento, imagen, categoría.
        productoRepository.save(new Producto("Polerón negro oversize", "Polerón de algodón con capucha.",
                29990, 0, "/img/poleron-negro.webp", polerones));
        productoRepository.save(new Producto("Polerón gris básico", "Polerón liso de corte regular.",
                24990, 20, "/img/poleron-basico-gris.jpg", polerones));
        productoRepository.save(new Producto("Polera blanca básica", "Algodón 100%, cuello redondo.",
                9990, 0, "/img/polera-blanca.jpg", poleras));
        productoRepository.save(new Producto("Polera estampada Fuma", "Estampado frontal en serigrafía.",
                12990, 15, "/img/polera-estampada-fuma.webp", poleras));
        productoRepository.save(new Producto("Jeans azul recto", "Corte recto de mezclilla.",
                32990, 0, "/img/pantalon-azul-recto.webp", pantalones));
        productoRepository.save(new Producto("Pantalón cargo beige", "Bolsillos laterales y ajuste en tobillo.",
                27990, 30, "/img/pantalon-cargo.avif", pantalones));
        productoRepository.save(new Producto("Gorro de lana", "Gorro tejido, talla única.",
                6990, 0, "/img/gorro-lana.jpg", accesorios));
        productoRepository.save(new Producto("Cinturón de cuero", "Hebilla metálica, 100 cm.",
                14990, 10, "/img/cinturon-de-cuero-negro.webp", accesorios));
    }
}
