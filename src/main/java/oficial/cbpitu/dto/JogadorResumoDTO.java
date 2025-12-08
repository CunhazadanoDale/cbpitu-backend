package oficial.cbpitu.dto;

import lombok.*;

/**
 * DTO resumido de jogador para evitar referências circulares.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JogadorResumoDTO {

    private Long id;
    private String nickname;
    private String laneLol;
}
