package oficial.cbpitu.mapper;

import oficial.cbpitu.dto.edicao.EdicaoDTO;
import oficial.cbpitu.model.Edicao;
import org.springframework.stereotype.Component;

@Component
public class EdicaoMapper {
    
    public EdicaoDTO toDTO(Edicao edicao) {
        if (edicao == null) return null;
        
        EdicaoDTO dto = new EdicaoDTO();
        dto.setId(edicao.getId());
        dto.setAno(edicao.getAno());
        dto.setNumeroEdicao(edicao.getNumeroEdicao());
        dto.setNome(edicao.getNome());
        dto.setNomeCompleto(edicao.getNomeCompleto());
        dto.setDataInicio(edicao.getDataInicio());
        dto.setDataFim(edicao.getDataFim());
        dto.setDescricao(edicao.getDescricao());
        dto.setNumeroCampeonatos(edicao.getCampeonatos() != null ? edicao.getCampeonatos().size() : 0);
        dto.setNumeroEscalacoes(edicao.getEscalacoes() != null ? edicao.getEscalacoes().size() : 0);
        
        return dto;
    }
    
    public Edicao toEntity(oficial.cbpitu.dto.edicao.CriarEdicaoDTO dto) {
        if (dto == null) return null;
        
        Edicao edicao = new Edicao();
        edicao.setAno(dto.getAno());
        edicao.setNumeroEdicao(dto.getNumeroEdicao() != null ? dto.getNumeroEdicao() : 1);
        edicao.setNome(dto.getNome());
        edicao.setDataInicio(dto.getDataInicio());
        edicao.setDataFim(dto.getDataFim());
        edicao.setDescricao(dto.getDescricao());
        
        return edicao;
    }
}
