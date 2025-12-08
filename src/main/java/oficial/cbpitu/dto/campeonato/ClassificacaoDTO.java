package oficial.cbpitu.dto.campeonato;

import lombok.*;
import oficial.cbpitu.dto.TimeResumoDTO;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassificacaoDTO {

    private Integer posicao;
    private TimeResumoDTO time;
    private Integer jogos;
    private Integer vitorias;
    private Integer empates;
    private Integer derrotas;
    private Integer golsPro;
    private Integer golsContra;
    private Integer saldoGols;
    private Integer pontos;
}
