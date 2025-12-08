package oficial.cbpitu.dto.campeonato;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para registrar resultado de uma partida.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultadoDTO {

    @NotNull(message = "Placar do time 1 é obrigatório")
    @Min(value = 0, message = "Placar não pode ser negativo")
    private Integer placarTime1;

    @NotNull(message = "Placar do time 2 é obrigatório")
    @Min(value = 0, message = "Placar não pode ser negativo")
    private Integer placarTime2;
}
