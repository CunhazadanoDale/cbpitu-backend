package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.PartidaDTO;
import oficial.cbpitu.model.Partida;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Partida e seus DTOs.
 */
@Component
@RequiredArgsConstructor
public class PartidaMapper {

    private final TimeMapper timeMapper;

    public PartidaDTO toDTO(Partida partida) {
        if (partida == null)
            return null;

        return PartidaDTO.builder()
                .id(partida.getId())
                .faseId(partida.getFase() != null ? partida.getFase().getId() : null)
                .grupoId(partida.getGrupo() != null ? partida.getGrupo().getId() : null)
                .time1(timeMapper.toResumoDTO(partida.getTime1()))
                .time2(timeMapper.toResumoDTO(partida.getTime2()))
                .placarTime1(partida.getPlacarTime1())
                .placarTime2(partida.getPlacarTime2())
                .dataHora(partida.getDataHora())
                .status(partida.getStatus())
                .rodada(partida.getRodada())
                .identificadorBracket(partida.getIdentificadorBracket())
                .vencedor(timeMapper.toResumoDTO(partida.getVencedor()))
                .nomeGrupo(partida.getGrupo() != null ? partida.getGrupo().getNome() : null)
                .build();
    }

    public List<PartidaDTO> toDTOList(List<Partida> partidas) {
        if (partidas == null)
            return List.of();

        return partidas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
