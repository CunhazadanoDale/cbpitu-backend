package oficial.cbpitu.dto.edicao;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CriarEscalacaoDTO {
    
    @NotNull(message = "ID do time é obrigatório")
    private Long timeId;
    
    @NotNull(message = "ID da edição é obrigatório")
    private Long edicaoId;
    
    private Set<Long> jogadoresIds;
    
    private Long capitaoId;
}
