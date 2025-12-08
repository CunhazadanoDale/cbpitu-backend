package oficial.cbpitu.dto;

import lombok.*;

/**
 * DTO para criação/atualização de jogador.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriarJogadorDTO {

    private String nickname;
    private String nomeReal;
    private String laneLol;
}
