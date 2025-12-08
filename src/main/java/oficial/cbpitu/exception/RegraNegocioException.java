package oficial.cbpitu.exception;

/**
 * Exceção para regras de negócio violadas.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
