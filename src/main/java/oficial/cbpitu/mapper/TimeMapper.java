package oficial.cbpitu.mapper;

import oficial.cbpitu.dto.TimeResumoDTO;
import oficial.cbpitu.model.Time;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre Time e seus DTOs.
 */
@Component
public class TimeMapper {

    public TimeResumoDTO toResumoDTO(Time time) {
        if (time == null)
            return null;

        return TimeResumoDTO.builder()
                .id(time.getId())
                .nomeTime(time.getNomeTime())
                .build();
    }
}
