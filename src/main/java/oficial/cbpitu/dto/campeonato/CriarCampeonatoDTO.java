package oficial.cbpitu.dto.campeonato;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO para criação/atualização de campeonato.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriarCampeonatoDTO {

    @NotBlank(message = "Nome do campeonato é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Integer limiteMaximoTimes;
}
