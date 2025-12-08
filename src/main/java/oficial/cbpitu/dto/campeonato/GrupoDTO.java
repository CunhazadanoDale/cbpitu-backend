package oficial.cbpitu.dto.campeonato;

import lombok.*;
import oficial.cbpitu.dto.TimeResumoDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrupoDTO {

    private Long id;
    private String nome;
    private List<TimeResumoDTO> times;
    private List<ClassificacaoDTO> classificacao;
}
