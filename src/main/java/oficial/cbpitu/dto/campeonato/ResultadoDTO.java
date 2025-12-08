package oficial.cbpitu.dto.campeonato;

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

    private Integer placarTime1;
    private Integer placarTime2;
}
