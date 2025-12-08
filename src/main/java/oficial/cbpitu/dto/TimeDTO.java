package oficial.cbpitu.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeDTO {

    private Long id;
    private String nomeTime;
    private String trofeus;
    private JogadorResumoDTO capitao;
    private List<JogadorResumoDTO> jogadores = new ArrayList<>();
}
