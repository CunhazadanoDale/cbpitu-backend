package oficial.cbpitu.exception;

/**
 * Exceção base para recursos não encontrados.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(String.format("%s não encontrado(a) com ID: %d", recurso, id));
    }
}
