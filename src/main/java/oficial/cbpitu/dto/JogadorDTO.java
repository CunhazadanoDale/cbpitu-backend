package oficial.cbpitu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oficial.cbpitu.model.Jogador;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JogadorDTO {

    private Long id;
    private String nickname;
    private String nomeReal;
    private String role;
}
