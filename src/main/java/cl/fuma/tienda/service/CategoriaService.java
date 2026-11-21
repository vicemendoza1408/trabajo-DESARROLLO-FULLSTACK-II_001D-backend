package cl.fuma.tienda.service;

import cl.fuma.tienda.exception.RecursoNoEncontradoException;
import cl.fuma.tienda.model.Categoria;
import cl.fuma.tienda.repository.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

// Lógica de las categorías. Los controladores (Fase 3) llaman a este servicio,
// y el servicio usa el repositorio para leer la base de datos.
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Spring entrega el repositorio solo (inyección de dependencias por constructor).
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    // Responde si existe una categoría con ese id (sin lanzar excepción).
    public boolean existe(String id) {
        return categoriaRepository.existsById(id);
    }

    // Si la categoría no existe, lanza una excepción que luego se convierte en un 404.
    public Categoria buscarPorId(String id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría " + id + "."));
    }
}
