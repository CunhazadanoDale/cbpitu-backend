package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.exception.RegraNegocioException;
import oficial.cbpitu.model.Campeonato;
import oficial.cbpitu.model.Jogador;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.repository.CampeonatoRepository;
import oficial.cbpitu.repository.JogadorRepository;
import oficial.cbpitu.repository.TimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeService {

    private final TimeRepository timeRepository;
    private final JogadorRepository jogadorRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final oficial.cbpitu.repository.EscalacaoRepository escalacaoRepository;
    private final oficial.cbpitu.repository.PartidaRepository partidaRepository;
    private final oficial.cbpitu.repository.GrupoRepository grupoRepository;

    public List<Time> listarTodos() {
        return timeRepository.findAll();
    }
    
    public org.springframework.data.domain.Page<Time> listarPaginado(org.springframework.data.domain.Pageable pageable) {
        return timeRepository.findAll(pageable);
    }
    
    public List<Time> listarTop() {
        return timeRepository.findTop4ByOrderByTrofeusDesc();
    }
    
    public oficial.cbpitu.dto.TimeDetalhesDTO buscarDetalhes(Long id) {
        Time time = buscarOuFalhar(id);
        List<oficial.cbpitu.model.Escalacao> historico = escalacaoRepository.findByTimeId(id);
        
        List<oficial.cbpitu.dto.TimeDetalhesDTO.HistoricoEdicaoDTO> historicoDTO = historico.stream()
            .sorted((e1, e2) -> {
                int anoCompare = e2.getEdicao().getAno().compareTo(e1.getEdicao().getAno());
                if (anoCompare != 0) return anoCompare;
                return e2.getEdicao().getId().compareTo(e1.getEdicao().getId());
            })
            .map(e -> oficial.cbpitu.dto.TimeDetalhesDTO.HistoricoEdicaoDTO.builder()
                .edicaoId(e.getEdicao().getId())
                .nomeEdicao(e.getEdicao().getNomeCompleto())
                .anoEdicao(e.getEdicao().getAno())
                .capitaoNaEdicao(e.getCapitao() != null ? toJogadorResumo(e.getCapitao()) : null)
                .jogadores(e.getJogadores().stream().map(this::toJogadorResumo).toList())
                .build())
            .toList();
            
        return oficial.cbpitu.dto.TimeDetalhesDTO.builder()
            .id(time.getId())
            .nomeTime(time.getNomeTime())
            .trofeus(time.getTrofeus())
            .capitaoAtual(time.getCapitao() != null ? toJogadorResumo(time.getCapitao()) : null)
            .historico(historicoDTO)
            .build();
    }
    
    private oficial.cbpitu.dto.JogadorResumoDTO toJogadorResumo(Jogador jogador) {
        return oficial.cbpitu.dto.JogadorResumoDTO.builder()
            .id(jogador.getId())
            .nickname(jogador.getNickname())
            .laneLol(jogador.getLaneLol())
            .titulos(jogador.getTitulos())
            .build();
    }

    public Optional<Time> buscarPorId(Long id) {
        return timeRepository.findById(id);
    }

    @Transactional
    public Time criar(Time time) {
        return timeRepository.save(time);
    }

    @Transactional
    public Time criar(String nomeTime, Integer trofeus, Long capitaoId) {
        Time time = new Time();
        time.setNomeTime(nomeTime);
        time.setTrofeus(trofeus != null ? trofeus : 0);

        if (capitaoId != null) {
            Jogador capitao = jogadorRepository.findById(capitaoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", capitaoId));
            time.setCapitao(capitao);
            time.getJogadores().add(capitao);
        }

        return timeRepository.save(time);
    }

    @Transactional
    public Time atualizar(Long id, String nomeTime, Integer trofeus, Long capitaoId) {
        Time time = buscarOuFalhar(id);

        time.setNomeTime(nomeTime);
        time.setTrofeus(trofeus != null ? trofeus : 0);

        if (capitaoId != null) {
            Jogador capitao = jogadorRepository.findById(capitaoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", capitaoId));
            time.setCapitao(capitao);
        }

        return timeRepository.save(time);
    }

    @Transactional
    public void deletar(Long id) {
        Time time = buscarOuFalhar(id);

        // 1. Remover de Confrontos/Partidas (Histórico de jogos se perde ou vira null?)
        // Decisão: Deletar partidas para manter integridade, pois partida com time null quebraria muita coisa
        List<oficial.cbpitu.model.Partida> partidas = partidaRepository.findByTimeId(id);
        partidaRepository.deleteAll(partidas);

        // 2. Remover de Grupos
        List<oficial.cbpitu.model.Grupo> gruposComTime = grupoRepository.findAll().stream()
                .filter(g -> g.getTimes().contains(time))
                .toList();
        for (oficial.cbpitu.model.Grupo g : gruposComTime) {
            g.removerTime(time);
            grupoRepository.save(g);
        }

        // 3. Remover de Escalações (Histórico de roster)
        List<oficial.cbpitu.model.Escalacao> escalacoes = escalacaoRepository.findByTimeId(id);
        escalacaoRepository.deleteAll(escalacoes);

        // 4. Remove o time de todos os campeonatos em que está inscrito
        List<Campeonato> campeonatos = campeonatoRepository.findByTimeParticipante(id);
        for (Campeonato campeonato : campeonatos) {
            campeonato.removerTime(time);
            // Se o time era o campeão, remove também
            if (time.equals(campeonato.getCampeao())) {
                campeonato.setCampeao(null);
            }
            campeonatoRepository.save(campeonato);
        }

        // 5. Limpa os jogadores do time (desassocia)
        time.getJogadores().clear();
        time.setCapitao(null);
        timeRepository.save(time);

        // Agora pode deletar
        timeRepository.delete(time);
    }

    // Gerenciamento de jogadores no time

    @Transactional
    public Time adicionarJogador(Long timeId, Long jogadorId) {
        Time time = buscarOuFalhar(timeId);
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", jogadorId));

        if (time.getJogadores().contains(jogador)) {
            throw new RegraNegocioException("Jogador já faz parte deste time");
        }

        time.getJogadores().add(jogador);
        return timeRepository.save(time);
    }

    @Transactional
    public Time removerJogador(Long timeId, Long jogadorId) {
        Time time = buscarOuFalhar(timeId);
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", jogadorId));

        if (!time.getJogadores().contains(jogador)) {
            throw new RegraNegocioException("Jogador não faz parte deste time");
        }

        time.getJogadores().remove(jogador);

        // Se o jogador removido era o capitão, remove também
        if (time.getCapitao() != null && time.getCapitao().equals(jogador)) {
            time.setCapitao(null);
        }

        return timeRepository.save(time);
    }

    @Transactional
    public Time definirCapitao(Long timeId, Long jogadorId) {
        Time time = buscarOuFalhar(timeId);
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", jogadorId));

        // O capitão precisa ser parte do time
        if (!time.getJogadores().contains(jogador)) {
            time.getJogadores().add(jogador);
        }

        time.setCapitao(jogador);
        return timeRepository.save(time);
    }

    // Helper
    private Time buscarOuFalhar(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Time", id));
    }
}
