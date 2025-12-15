package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.edicao.CriarEdicaoDTO;
import oficial.cbpitu.dto.edicao.EdicaoDTO;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.exception.RegraNegocioException;
import oficial.cbpitu.mapper.EdicaoMapper;
import oficial.cbpitu.model.Edicao;
import oficial.cbpitu.repository.EdicaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EdicaoService {
    
    private final EdicaoRepository edicaoRepository;
    private final EdicaoMapper edicaoMapper;
    
    public List<EdicaoDTO> listarTodas() {
        return edicaoRepository.findAllByOrderByAnoDescNumeroEdicaoDesc()
            .stream()
            .map(edicaoMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public EdicaoDTO buscarPorId(Long id) {
        Edicao edicao = buscarOuFalhar(id);
        return edicaoMapper.toDTO(edicao);
    }
    
    public List<EdicaoDTO> buscarPorAno(Integer ano) {
        return edicaoRepository.findByAnoOrderByNumeroEdicaoAsc(ano)
            .stream()
            .map(edicaoMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public EdicaoDTO criar(CriarEdicaoDTO dto) {
        // Validar se já existe edição com mesmo ano e número
        if (edicaoRepository.existsByAnoAndNumeroEdicao(dto.getAno(), dto.getNumeroEdicao())) {
            throw new RegraNegocioException(
                "Já existe uma edição para o ano " + dto.getAno() + " com número " + dto.getNumeroEdicao()
            );
        }
        
        Edicao edicao = edicaoMapper.toEntity(dto);
        edicao = edicaoRepository.save(edicao);
        
        return edicaoMapper.toDTO(edicao);
    }
    
    public EdicaoDTO atualizar(Long id, CriarEdicaoDTO dto) {
        Edicao edicao = buscarOuFalhar(id);
        
        // Validar se já existe outra edição com mesmo ano e número
        edicaoRepository.findByAnoAndNumeroEdicao(dto.getAno(), dto.getNumeroEdicao())
            .ifPresent(e -> {
                if (!e.getId().equals(id)) {
                    throw new RegraNegocioException(
                        "Já existe uma edição para o ano " + dto.getAno() + " com número " + dto.getNumeroEdicao()
                    );
                }
            });
        
        edicao.setAno(dto.getAno());
        edicao.setNumeroEdicao(dto.getNumeroEdicao() != null ? dto.getNumeroEdicao() : 1);
        edicao.setNome(dto.getNome());
        edicao.setDataInicio(dto.getDataInicio());
        edicao.setDataFim(dto.getDataFim());
        edicao.setDescricao(dto.getDescricao());
        
        edicao = edicaoRepository.save(edicao);
        return edicaoMapper.toDTO(edicao);
    }
    
    public void deletar(Long id) {
        Edicao edicao = buscarOuFalhar(id);
        
        if (!edicao.getCampeonatos().isEmpty()) {
            throw new RegraNegocioException(
                "Não é possível excluir uma edição que possui campeonatos associados"
            );
        }
        
        edicaoRepository.delete(edicao);
    }
    
    public Edicao buscarOuFalhar(Long id) {
        return edicaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Edição não encontrada com ID: " + id));
    }
}
