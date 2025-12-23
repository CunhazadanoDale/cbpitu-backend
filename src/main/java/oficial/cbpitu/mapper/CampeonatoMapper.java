package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.CampeonatoDTO;
import oficial.cbpitu.dto.campeonato.CriarCampeonatoDTO;
import oficial.cbpitu.model.Campeonato;
import oficial.cbpitu.model.Edicao;
import oficial.cbpitu.repository.EdicaoRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final EdicaoRepository edicaoRepository;

    public CampeonatoDTO toDTO(Campeonato campeonato) {
        if (campeonato == null)
            return null;

        CampeonatoDTO.CampeonatoDTOBuilder builder = CampeonatoDTO.builder()
                .id(campeonato.getId())
                .nome(campeonato.getNome())
                .descricao(campeonato.getDescricao())
                .dataInicio(campeonato.getDataInicio())
                .dataFim(campeonato.getDataFim())
                .status(campeonato.getStatus())
                .limiteMaximoTimes(campeonato.getLimiteMaximoTimes())
                .numeroTimesInscritos(campeonato.getNumeroTimesInscritos())
                .timesParticipantes(timeMapper.toResumoDTOList(new ArrayList<>(campeonato.getTimesParticipantes())))
                .fases(faseMapper.toDTOList(campeonato.getFases()))
                .campeao(timeMapper.toResumoDTO(campeonato.getCampeao()));
        
        // Adiciona informações da edição, se houver
        if (campeonato.getEdicao() != null) {
            builder.edicaoId(campeonato.getEdicao().getId());
            builder.edicaoNome(campeonato.getEdicao().getNomeCompleto());
        }
        
        return builder.build();
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
        
        // Associa à edição, se fornecida
        if (dto.getEdicaoId() != null) {
            Edicao edicao = edicaoRepository.findById(dto.getEdicaoId()).orElse(null);
            campeonato.setEdicao(edicao);
        }
        
        return campeonato;
    }
}

