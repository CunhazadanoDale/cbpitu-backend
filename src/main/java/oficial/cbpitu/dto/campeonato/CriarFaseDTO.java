package oficial.cbpitu.dto.campeonato;

import lombok.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;

/**
 * DTO para adicionar uma fase ao campeonato.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriarFaseDTO {

    private String nome;
    private FormatoCompeticao formato;
    private Integer classificadosNecessarios;
    private Integer rodadasTotais; // Para sistema suíço
}
