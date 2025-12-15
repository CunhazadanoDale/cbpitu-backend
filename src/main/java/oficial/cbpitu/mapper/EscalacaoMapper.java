package oficial.cbpitu.mapper;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.JogadorResumoDTO;
import oficial.cbpitu.dto.TimeResumoDTO;
import oficial.cbpitu.dto.edicao.EscalacaoDTO;
import oficial.cbpitu.model.Escalacao;
import oficial.cbpitu.model.Jogador;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EscalacaoMapper {
    
    public EscalacaoDTO toDTO(Escalacao escalacao) {
        if (escalacao == null) return null;
        
        EscalacaoDTO dto = new EscalacaoDTO();
        dto.setId(escalacao.getId());
        
        // Time resumo
        if (escalacao.getTime() != null) {
            TimeResumoDTO timeResumo = new TimeResumoDTO();
            timeResumo.setId(escalacao.getTime().getId());
            timeResumo.setNomeTime(escalacao.getTime().getNomeTime());
            dto.setTime(timeResumo);
        }
        
        // Edicao info
        if (escalacao.getEdicao() != null) {
            dto.setEdicaoId(escalacao.getEdicao().getId());
            dto.setEdicaoAno(escalacao.getEdicao().getAno());
            dto.setEdicaoNome(escalacao.getEdicao().getNomeCompleto());
        }
        
        // Jogadores
        if (escalacao.getJogadores() != null) {
            List<JogadorResumoDTO> jogadoresDTO = escalacao.getJogadores().stream()
                .map(this::toJogadorResumo)
                .collect(Collectors.toList());
            dto.setJogadores(jogadoresDTO);
        }
        
        // Capitão
        if (escalacao.getCapitao() != null) {
            dto.setCapitao(toJogadorResumo(escalacao.getCapitao()));
        }
        
        dto.setNumeroJogadores(escalacao.getNumeroJogadores());
        
        return dto;
    }
    
    private JogadorResumoDTO toJogadorResumo(Jogador jogador) {
        return JogadorResumoDTO.builder()
            .id(jogador.getId())
            .nickname(jogador.getNickname())
            .laneLol(jogador.getLaneLol())
            .build();
    }
}
