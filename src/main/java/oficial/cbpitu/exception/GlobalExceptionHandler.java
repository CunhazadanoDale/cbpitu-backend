package oficial.cbpitu.exception;

import oficial.cbpitu.dto.ErroDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tratamento global de exceções para toda a API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex, WebRequest request) {

        ErroDTO erro = ErroDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .erro("Recurso não encontrado")
                .mensagem(ex.getMessage())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroDTO> handleRegraNegocio(
            RegraNegocioException ex, WebRequest request) {

        ErroDTO erro = ErroDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .erro("Regra de negócio violada")
                .mensagem(ex.getMessage())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(OperacaoInvalidaException.class)
    public ResponseEntity<ErroDTO> handleOperacaoInvalida(
            OperacaoInvalidaException ex, WebRequest request) {

        ErroDTO erro = ErroDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .erro("Operação não permitida")
                .mensagem(ex.getMessage())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<ErroDTO.CampoErroDTO> camposComErro = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toCampoErroDTO)
                .collect(Collectors.toList());

        ErroDTO erro = ErroDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .erro("Erro de validação")
                .mensagem("Um ou mais campos possuem valores inválidos")
                .path(getPath(request))
                .campos(camposComErro)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroDTO> handleGenericException(
            Exception ex, WebRequest request) {

        ErroDTO erro = ErroDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .erro("Erro interno do servidor")
                .mensagem("Ocorreu um erro inesperado. Tente novamente mais tarde.")
                .path(getPath(request))
                .build();

        // Log do erro real para debug
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    private ErroDTO.CampoErroDTO toCampoErroDTO(FieldError fieldError) {
        return ErroDTO.CampoErroDTO.builder()
                .campo(fieldError.getField())
                .mensagem(fieldError.getDefaultMessage())
                .build();
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
