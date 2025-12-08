package oficial.cbpitu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oficial.cbpitu.model.Time;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeDTO {

    private Long id;
    private String nomeTime;
    private String trofeus;
    private JogadorDTO capitao;
    private Set<JogadorDTO> jogadores = new HashSet<>();
}
