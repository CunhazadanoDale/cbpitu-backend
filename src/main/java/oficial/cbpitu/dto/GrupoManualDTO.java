package oficial.cbpitu.dto;

import lombok.Data;
import java.util.List;

@Data
public class GrupoManualDTO {
    private String nome;
    private List<Long> timesIds;
}
