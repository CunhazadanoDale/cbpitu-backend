package oficial.cbpitu.dto.campeonato;

import lombok.*;
import oficial.cbpitu.dto.TimeResumoDTO;
import oficial.cbpitu.model.enums.StatusCampeonato;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampeonatoDTO {

    private Long id;
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private StatusCampeonato status;
    private Integer limiteMaximoTimes;
    private Integer numeroTimesInscritos;
    private List<FaseDTO> fases;
    private TimeResumoDTO campeao;
}
