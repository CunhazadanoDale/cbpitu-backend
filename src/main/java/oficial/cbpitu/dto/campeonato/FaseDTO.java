package oficial.cbpitu.dto.campeonato;

import lombok.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaseDTO {

    private Long id;
    private String nome;
    private Integer ordem;
    private FormatoCompeticao formato;
    private Integer classificadosNecessarios;
    private Integer rodadasTotais;
    private Boolean finalizada;
    private List<GrupoDTO> grupos;
    private List<PartidaDTO> partidas;
}
