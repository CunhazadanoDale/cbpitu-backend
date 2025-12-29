package oficial.cbpitu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JogadorDetalhesDTO {
    private Long id;
    private String nickname;
    private String nomeReal;
    private String laneLol;
    private Integer titulos;
    
    private List<HistoricoItemDTO> historico;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HistoricoItemDTO {
        private String nomeTime;
        private Long edicaoId;
        private String nomeEdicao;
        private Integer anoEdicao;
        private boolean isCapitao;
    }
}
