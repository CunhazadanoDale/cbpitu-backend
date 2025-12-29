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
public class TimeDetalhesDTO {
    private Long id;
    private String nomeTime;
    private Integer trofeus;
    private JogadorResumoDTO capitaoAtual;
    
    // Histórico de participações em edições
    private List<HistoricoEdicaoDTO> historico;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HistoricoEdicaoDTO {
        private Long edicaoId;
        private String nomeEdicao;
        private Integer anoEdicao;
        private JogadorResumoDTO capitaoNaEdicao;
        private List<JogadorResumoDTO> jogadores;
    }
}
