package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.TimeDTO;
import oficial.cbpitu.dto.TimeResumoDTO;
import oficial.cbpitu.model.Time;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Time e seus DTOs.
 */
@Component
@RequiredArgsConstructor
public class TimeMapper {

    private final JogadorMapper jogadorMapper;

    public TimeDTO toDTO(Time time) {
        if (time == null)
            return null;

        return TimeDTO.builder()
                .id(time.getId())
                .nomeTime(time.getNomeTime())
                .trofeus(time.getTrofeus())
                .capitao(jogadorMapper.toResumoDTO(time.getCapitao()))
                .jogadores(new ArrayList<>(time.getJogadores()).stream()
                        .map(jogadorMapper::toResumoDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public TimeResumoDTO toResumoDTO(Time time) {
        if (time == null)
            return null;

        return TimeResumoDTO.builder()
                .id(time.getId())
                .nomeTime(time.getNomeTime())
                .trofeus(time.getTrofeus())
                .build();
    }

    public List<TimeDTO> toDTOList(List<Time> times) {
        if (times == null)
            return List.of();

        return times.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TimeResumoDTO> toResumoDTOList(List<Time> times) {
        if (times == null)
            return List.of();

        return times.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
}
