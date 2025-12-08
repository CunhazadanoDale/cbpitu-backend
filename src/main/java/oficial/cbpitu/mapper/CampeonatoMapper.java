package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.CampeonatoDTO;
import oficial.cbpitu.dto.campeonato.CriarCampeonatoDTO;
import oficial.cbpitu.model.Campeonato;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre Campeonato e seus DTOs.
 */
@Component
@RequiredArgsConstructor
public class CampeonatoMapper {

    private final FaseMapper faseMapper;
    private final TimeMapper timeMapper;

    public CampeonatoDTO toDTO(Campeonato campeonato) {
        if (campeonato == null)
            return null;

        return CampeonatoDTO.builder()
                .id(campeonato.getId())
                .nome(campeonato.getNome())
                .descricao(campeonato.getDescricao())
                .dataInicio(campeonato.getDataInicio())
                .dataFim(campeonato.getDataFim())
                .status(campeonato.getStatus())
                .limiteMaximoTimes(campeonato.getLimiteMaximoTimes())
                .numeroTimesInscritos(campeonato.getNumeroTimesInscritos())
                .fases(faseMapper.toDTOList(campeonato.getFases()))
                .campeao(timeMapper.toResumoDTO(campeonato.getCampeao()))
                .build();
    }

    public List<CampeonatoDTO> toDTOList(List<Campeonato> campeonatos) {
        if (campeonatos == null)
            return List.of();

        return campeonatos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte DTO de criação para entidade.
     */
    public Campeonato toEntity(CriarCampeonatoDTO dto) {
        if (dto == null)
            return null;

        Campeonato campeonato = new Campeonato();
        campeonato.setNome(dto.getNome());
        campeonato.setDescricao(dto.getDescricao());
        campeonato.setDataInicio(dto.getDataInicio());
        campeonato.setDataFim(dto.getDataFim());
        campeonato.setLimiteMaximoTimes(dto.getLimiteMaximoTimes());
        return campeonato;
    }
}
