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

    public List<Time> listarTodos() {
        return timeRepository.findAll();
    }

    public Optional<Time> buscarPorId(Long id) {
        return timeRepository.findById(id);
    }

    @Transactional
    public Time criar(Time time) {
        return timeRepository.save(time);
    }

    @Transactional
    public Time criar(String nomeTime, String trofeus, Long capitaoId) {
        Time time = new Time();
        time.setNomeTime(nomeTime);
        time.setTrofeus(trofeus);

        if (capitaoId != null) {
            Jogador capitao = jogadorRepository.findById(capitaoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", capitaoId));
            time.setCapitao(capitao);
            time.getJogadores().add(capitao);
        }

        return timeRepository.save(time);
    }

    @Transactional
    public Time atualizar(Long id, String nomeTime, String trofeus, Long capitaoId) {
        Time time = buscarOuFalhar(id);

        time.setNomeTime(nomeTime);
        time.setTrofeus(trofeus);

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

        // Remove o time de todos os campeonatos em que está inscrito
        List<Campeonato> campeonatos = campeonatoRepository.findByTimeParticipante(id);
        for (Campeonato campeonato : campeonatos) {
            campeonato.removerTime(time);
            // Se o time era o campeão, remove também
            if (time.equals(campeonato.getCampeao())) {
                campeonato.setCampeao(null);
            }
            campeonatoRepository.save(campeonato);
        }

        // Limpa os jogadores do time
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
