package oficial.cbpitu.dto.campeonato;

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

    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Integer limiteMaximoTimes;
}
