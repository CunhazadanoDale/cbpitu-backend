package oficial.cbpitu.dto.campeonato;

import lombok.*;
import oficial.cbpitu.dto.TimeResumoDTO;
import oficial.cbpitu.model.enums.StatusPartida;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartidaDTO {

    private Long id;
    private TimeResumoDTO time1;
    private TimeResumoDTO time2;
    private Integer placarTime1;
    private Integer placarTime2;
    private LocalDateTime dataHora;
    private StatusPartida status;
    private Integer rodada;
    private String identificadorBracket;
    private TimeResumoDTO vencedor;
    private String nomeGrupo;
}
