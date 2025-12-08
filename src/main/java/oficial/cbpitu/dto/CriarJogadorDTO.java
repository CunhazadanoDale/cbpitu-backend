package oficial.cbpitu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Nickname é obrigatório")
    @Size(min = 2, max = 50, message = "Nickname deve ter entre 2 e 50 caracteres")
    private String nickname;

    @Size(max = 100, message = "Nome real deve ter no máximo 100 caracteres")
    private String nomeReal;

    @NotBlank(message = "Lane é obrigatória")
    @Pattern(regexp = "^(TOP|JUNGLE|MID|ADC|SUPPORT)$", message = "Lane deve ser: TOP, JUNGLE, MID, ADC ou SUPPORT")
    private String laneLol;
}
