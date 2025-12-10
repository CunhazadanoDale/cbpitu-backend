package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.GrupoDTO;
import oficial.cbpitu.model.Grupo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Grupo e seus DTOs.
 */
@Component
@RequiredArgsConstructor
public class GrupoMapper {

    private final TimeMapper timeMapper;
    private final oficial.cbpitu.service.strategy.FaseDeGruposStrategy faseDeGruposStrategy;

    public GrupoDTO toDTO(Grupo grupo) {
        if (grupo == null)
            return null;

        return GrupoDTO.builder()
                .id(grupo.getId())
                .nome(grupo.getNome())
                .times(grupo.getTimes().stream()
                        .map(timeMapper::toResumoDTO)
                        .collect(Collectors.toList()))
                .classificacao(toClassificacaoDTOList(faseDeGruposStrategy.calcularTabelaGrupo(grupo)))
                .build();
    }

    public List<GrupoDTO> toDTOList(List<Grupo> grupos) {
        if (grupos == null)
            return List.of();

        return grupos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private List<oficial.cbpitu.dto.campeonato.ClassificacaoDTO> toClassificacaoDTOList(
            List<oficial.cbpitu.service.strategy.FaseDeGruposStrategy.ClassificacaoGrupo> classificacao) {
        if (classificacao == null)
            return List.of();

        return classificacao.stream()
                .map(c -> oficial.cbpitu.dto.campeonato.ClassificacaoDTO.builder()
                        .time(timeMapper.toResumoDTO(c.getTime()))
                        .jogos(c.getJogos())
                        .vitorias(c.getVitorias())
                        .empates(c.getEmpates())
                        .derrotas(c.getDerrotas())
                        .golsPro(c.getGolsPro())
                        .golsContra(c.getGolsContra())
                        .saldoGols(c.getSaldoGols())
                        .pontos(c.getPontos())
                        .posicao(0) // Posição calculada no front ou pelo índice
                        .build())
                .collect(Collectors.toList());
    }
}
