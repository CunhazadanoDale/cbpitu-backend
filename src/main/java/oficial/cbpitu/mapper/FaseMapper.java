package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.FaseDTO;
import oficial.cbpitu.model.Fase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Fase e seus DTOs.
 */
@Component
@RequiredArgsConstructor
public class FaseMapper {

    private final GrupoMapper grupoMapper;
    private final PartidaMapper partidaMapper;

    public FaseDTO toDTO(Fase fase) {
        if (fase == null)
            return null;

        return FaseDTO.builder()
                .id(fase.getId())
                .nome(fase.getNome())
                .ordem(fase.getOrdem())
                .formato(fase.getFormato())
                .classificadosNecessarios(fase.getClassificadosNecessarios())
                .rodadasTotais(fase.getRodadasTotais())
                .finalizada(fase.getFinalizada())
                .grupos(grupoMapper.toDTOList(fase.getGrupos()))
                .partidas(partidaMapper.toDTOList(fase.getPartidas()))
                .build();
    }

    public List<FaseDTO> toDTOList(List<Fase> fases) {
        if (fases == null)
            return List.of();

        return fases.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
