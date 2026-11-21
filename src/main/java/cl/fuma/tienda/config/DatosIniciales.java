package cl.fuma.tienda.config;

import cl.fuma.tienda.model.Categoria;
import cl.fuma.tienda.model.Producto;
import cl.fuma.tienda.repository.CategoriaRepository;
import cl.fuma.tienda.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Carga los datos iniciales de la tienda (las mismas categorías y productos del frontend)
// la primera vez que se levanta el backend. CommandLineRunner se ejecuta al arrancar la aplicación.
// Los usuarios de prueba se agregan en la Fase 4, cuando ya exista el cifrado de contraseñas.
@Component
public class DatosIniciales implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    // Spring entrega los repositorios solos (inyección de dependencias por constructor).
    public DatosIniciales(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        // Si ya hay categorías, la base ya tiene datos y no se vuelve a cargar nada.
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
