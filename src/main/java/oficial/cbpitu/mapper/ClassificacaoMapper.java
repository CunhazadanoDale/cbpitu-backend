package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.ClassificacaoDTO;
import oficial.cbpitu.service.strategy.FaseDeGruposStrategy.ClassificacaoGrupo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para conversão de dados de classificação.
 */
@Component
@RequiredArgsConstructor
public class ClassificacaoMapper {

    private final TimeMapper timeMapper;

    public ClassificacaoDTO toDTO(ClassificacaoGrupo classificacao, int posicao) {
        if (classificacao == null)
            return null;

        return ClassificacaoDTO.builder()
                .posicao(posicao)
                .time(timeMapper.toResumoDTO(classificacao.getTime()))
                .jogos(classificacao.getJogos())
                .vitorias(classificacao.getVitorias())
                .empates(classificacao.getEmpates())
                .derrotas(classificacao.getDerrotas())
                .golsPro(classificacao.getGolsPro())
                .golsContra(classificacao.getGolsContra())
                .saldoGols(classificacao.getSaldoGols())
                .pontos(classificacao.getPontos())
                .build();
    }

    public List<ClassificacaoDTO> toDTOList(List<ClassificacaoGrupo> tabela) {
        if (tabela == null)
            return List.of();

        List<ClassificacaoDTO> resultado = new ArrayList<>();
        for (int i = 0; i < tabela.size(); i++) {
            resultado.add(toDTO(tabela.get(i), i + 1));
        }
        return resultado;
    }
}
