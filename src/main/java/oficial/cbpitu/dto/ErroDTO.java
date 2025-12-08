package oficial.cbpitu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO padronizado para respostas de erro da API.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroDTO {

    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
    private String path;

    @Builder.Default
    private List<CampoErroDTO> campos = new ArrayList<>();

    /**
     * Representa um erro de validação em um campo específico.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CampoErroDTO {
        private String campo;
        private String mensagem;
    }
}
