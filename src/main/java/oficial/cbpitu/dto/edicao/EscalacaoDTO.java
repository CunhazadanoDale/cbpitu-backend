package oficial.cbpitu.dto.edicao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import oficial.cbpitu.dto.JogadorResumoDTO;
import oficial.cbpitu.dto.TimeResumoDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EscalacaoDTO {
    private Long id;
    private TimeResumoDTO time;
    private Long edicaoId;
    private Integer edicaoAno;
    private String edicaoNome;
    private List<JogadorResumoDTO> jogadores;
    private JogadorResumoDTO capitao;
    private Integer numeroJogadores;
}
