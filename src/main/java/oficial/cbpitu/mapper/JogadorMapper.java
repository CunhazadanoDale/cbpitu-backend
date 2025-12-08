package oficial.cbpitu.mapper;

import oficial.cbpitu.dto.CriarJogadorDTO;
import oficial.cbpitu.dto.JogadorDTO;
import oficial.cbpitu.dto.JogadorResumoDTO;
import oficial.cbpitu.dto.TimeResumoDTO;
import oficial.cbpitu.model.Jogador;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Jogador e seus DTOs.
 */
@Component
public class JogadorMapper {

    public JogadorDTO toDTO(Jogador jogador) {
        if (jogador == null)
            return null;

        return JogadorDTO.builder()
                .id(jogador.getId())
                .nickname(jogador.getNickname())
                .nomeReal(jogador.getNomeReal())
                .laneLol(jogador.getLaneLol())
                .times(jogador.getTimes().stream()
                        .map(t -> TimeResumoDTO.builder()
                                .id(t.getId())
                                .nomeTime(t.getNomeTime())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public JogadorResumoDTO toResumoDTO(Jogador jogador) {
        if (jogador == null)
            return null;

        return JogadorResumoDTO.builder()
                .id(jogador.getId())
                .nickname(jogador.getNickname())
                .laneLol(jogador.getLaneLol())
                .build();
    }

    public List<JogadorDTO> toDTOList(List<Jogador> jogadores) {
        if (jogadores == null)
            return List.of();

        return jogadores.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<JogadorResumoDTO> toResumoDTOList(List<Jogador> jogadores) {
        if (jogadores == null)
            return List.of();

        return jogadores.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }

    public Jogador toEntity(CriarJogadorDTO dto) {
        if (dto == null)
            return null;

        Jogador jogador = new Jogador();
        jogador.setNickname(dto.getNickname());
        jogador.setNomeReal(dto.getNomeReal());
        jogador.setLaneLol(dto.getLaneLol());
        return jogador;
    }
}
