package cl.fuma.tienda.exception;

// Se lanza cuando se busca algo que no existe (un producto, una categoría o una boleta con ese id).
// En la Fase 3 se convertirá en una respuesta HTTP 404 (No encontrado).
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
