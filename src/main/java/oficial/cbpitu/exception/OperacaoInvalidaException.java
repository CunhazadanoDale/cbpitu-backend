package oficial.cbpitu.exception;

/**
 * Exceção para operações não permitidas no estado atual.
 */
public class OperacaoInvalidaException extends RuntimeException {

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
