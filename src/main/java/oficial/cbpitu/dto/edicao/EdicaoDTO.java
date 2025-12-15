package oficial.cbpitu.dto.edicao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EdicaoDTO {
    private Long id;
    private Integer ano;
    private Integer numeroEdicao;
    private String nome;
    private String nomeCompleto;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String descricao;
    private Integer numeroCampeonatos;
    private Integer numeroEscalacoes;
}
