package oficial.cbpitu.dto;

import lombok.*;

/**
 * DTO para criação/atualização de time.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriarTimeDTO {

    private String nomeTime;
    private String trofeus;
    private Long capitaoId;
}
