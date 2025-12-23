package oficial.cbpitu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para criação/atualização de time.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriarTimeDTO {

    @NotBlank(message = "Nome do time é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do time deve ter entre 2 e 100 caracteres")
    private String nomeTime;

    // Quantidade de troféus (opcional, default 0)
    private Integer trofeus;

    private Long capitaoId;
}
