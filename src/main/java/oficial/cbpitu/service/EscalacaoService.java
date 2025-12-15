package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.edicao.CriarEscalacaoDTO;
import oficial.cbpitu.dto.edicao.EscalacaoDTO;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.exception.RegraNegocioException;
import oficial.cbpitu.mapper.EscalacaoMapper;
import oficial.cbpitu.model.Edicao;
import oficial.cbpitu.model.Escalacao;
import oficial.cbpitu.model.Jogador;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.repository.EdicaoRepository;
import oficial.cbpitu.repository.EscalacaoRepository;
import oficial.cbpitu.repository.JogadorRepository;
import oficial.cbpitu.repository.TimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EscalacaoService {
    
    private final EscalacaoRepository escalacaoRepository;
    private final EdicaoRepository edicaoRepository;
    private final TimeRepository timeRepository;
    private final JogadorRepository jogadorRepository;
    private final EscalacaoMapper escalacaoMapper;
    
    public List<EscalacaoDTO> listarTodas() {
        return escalacaoRepository.findAll()
            .stream()
            .map(escalacaoMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public EscalacaoDTO buscarPorId(Long id) {
        Escalacao escalacao = buscarOuFalhar(id);
        return escalacaoMapper.toDTO(escalacao);
    }
    
    public List<EscalacaoDTO> buscarHistoricoTime(Long timeId) {
        // Validar se time existe
        if (!timeRepository.existsById(timeId)) {
            throw new RecursoNaoEncontradoException("Time não encontrado com ID: " + timeId);
        }
        
        return escalacaoRepository.findByTimeIdOrderByEdicaoAnoDescEdicaoNumeroEdicaoDesc(timeId)
            .stream()
            .map(escalacaoMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<EscalacaoDTO> buscarPorEdicao(Long edicaoId) {
        // Validar se edição existe
        if (!edicaoRepository.existsById(edicaoId)) {
            throw new RecursoNaoEncontradoException("Edição não encontrada com ID: " + edicaoId);
        }
        
        return escalacaoRepository.findByEdicaoId(edicaoId)
            .stream()
            .map(escalacaoMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public EscalacaoDTO criar(CriarEscalacaoDTO dto) {
        // Validar se já existe escalação para este time nesta edição
        if (escalacaoRepository.existsByTimeIdAndEdicaoId(dto.getTimeId(), dto.getEdicaoId())) {
            throw new RegraNegocioException(
                "Já existe uma escalação para este time nesta edição"
            );
        }
        
        Time time = timeRepository.findById(dto.getTimeId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Time não encontrado com ID: " + dto.getTimeId()));
        
        Edicao edicao = edicaoRepository.findById(dto.getEdicaoId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Edição não encontrada com ID: " + dto.getEdicaoId()));
        
        Escalacao escalacao = new Escalacao();
        escalacao.setTime(time);
        escalacao.setEdicao(edicao);
        
        // Adicionar jogadores
        if (dto.getJogadoresIds() != null && !dto.getJogadoresIds().isEmpty()) {
            Set<Jogador> jogadores = new HashSet<>();
            for (Long jogadorId : dto.getJogadoresIds()) {
                Jogador jogador = jogadorRepository.findById(jogadorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador não encontrado com ID: " + jogadorId));
                jogadores.add(jogador);
            }
            escalacao.setJogadores(jogadores);
        }
        
        // Definir capitão
        if (dto.getCapitaoId() != null) {
            Jogador capitao = jogadorRepository.findById(dto.getCapitaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Capitão não encontrado com ID: " + dto.getCapitaoId()));
            escalacao.setCapitao(capitao);
        }
        
        escalacao = escalacaoRepository.save(escalacao);
        return escalacaoMapper.toDTO(escalacao);
    }
    
    public EscalacaoDTO atualizar(Long id, CriarEscalacaoDTO dto) {
        Escalacao escalacao = buscarOuFalhar(id);
        
        // Atualizar jogadores
        if (dto.getJogadoresIds() != null) {
            Set<Jogador> jogadores = new HashSet<>();
            for (Long jogadorId : dto.getJogadoresIds()) {
                Jogador jogador = jogadorRepository.findById(jogadorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador não encontrado com ID: " + jogadorId));
                jogadores.add(jogador);
            }
            escalacao.setJogadores(jogadores);
        }
        
        // Atualizar capitão
        if (dto.getCapitaoId() != null) {
            Jogador capitao = jogadorRepository.findById(dto.getCapitaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Capitão não encontrado com ID: " + dto.getCapitaoId()));
            escalacao.setCapitao(capitao);
        } else {
            escalacao.setCapitao(null);
        }
        
        escalacao = escalacaoRepository.save(escalacao);
        return escalacaoMapper.toDTO(escalacao);
    }
    
    public void deletar(Long id) {
        Escalacao escalacao = buscarOuFalhar(id);
        escalacaoRepository.delete(escalacao);
    }
    
    private Escalacao buscarOuFalhar(Long id) {
        return escalacaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Escalação não encontrada com ID: " + id));
    }
}
