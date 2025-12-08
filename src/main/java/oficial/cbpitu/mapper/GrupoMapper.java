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

    public GrupoDTO toDTO(Grupo grupo) {
        if (grupo == null)
            return null;

        return GrupoDTO.builder()
                .id(grupo.getId())
                .nome(grupo.getNome())
                .times(grupo.getTimes().stream()
                        .map(timeMapper::toResumoDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<GrupoDTO> toDTOList(List<Grupo> grupos) {
        if (grupos == null)
            return List.of();

        return grupos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
