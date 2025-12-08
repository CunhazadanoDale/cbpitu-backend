package oficial.cbpitu.dto.campeonato;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;

/**
 * DTO para adicionar uma fase ao campeonato.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriarFaseDTO {

    @NotBlank(message = "Nome da fase é obrigatório")
    private String nome;

    @NotNull(message = "Formato da competição é obrigatório")
    private FormatoCompeticao formato;

    @Min(value = 1, message = "Classificados necessários deve ser pelo menos 1")
    private Integer classificadosNecessarios;

    @Min(value = 1, message = "Rodadas totais deve ser pelo menos 1")
    private Integer rodadasTotais;
}
