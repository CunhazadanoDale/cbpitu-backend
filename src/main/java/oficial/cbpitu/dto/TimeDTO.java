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

    public TimeDTO(Time entity) {
        this.id = entity.getId();
        this.nomeTime = entity.getNomeTime();
        this.trofeus = entity.getTrofeus();
        if (entity.getCapitao() != null) {
            this.capitao = new JogadorDTO(entity.getCapitao());
        }
        this.jogadores = entity.getJogadores().stream().map(JogadorDTO::new).collect(Collectors.toSet());
    }
}
