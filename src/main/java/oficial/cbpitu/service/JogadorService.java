package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.model.Jogador;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.repository.JogadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JogadorService {

    private final JogadorRepository jogadorRepository;

    public List<Jogador> listarTodos() {
        return jogadorRepository.findAll();
    }

    public Optional<Jogador> buscarPorId(Long id) {
        return jogadorRepository.findById(id);
    }

    @Transactional
    public Jogador criar(Jogador jogador) {
        return jogadorRepository.save(jogador);
    }

    @Transactional
    public Jogador atualizar(Long id, Jogador dados) {
        Jogador jogador = buscarOuFalhar(id);

        jogador.setNickname(dados.getNickname());
        jogador.setNomeReal(dados.getNomeReal());
        jogador.setLaneLol(dados.getLaneLol());

        return jogadorRepository.save(jogador);
    }

    @Transactional
    public void deletar(Long id) {
        Jogador jogador = buscarOuFalhar(id);

        // Remove jogador de todos os times antes de deletar
        for (Time time : jogador.getTimes()) {
            time.getJogadores().remove(jogador);
            if (time.getCapitao() != null && time.getCapitao().equals(jogador)) {
                time.setCapitao(null);
            }
        }

        jogadorRepository.delete(jogador);
    }

    public List<Jogador> buscarPorLane(String lane) {
        return jogadorRepository.findAll().stream()
                .filter(j -> lane.equalsIgnoreCase(j.getLaneLol()))
                .toList();
    }

    public List<Jogador> buscarSemTime() {
        return jogadorRepository.findAll().stream()
                .filter(j -> j.getTimes().isEmpty())
                .toList();
    }

    // Helper
    private Jogador buscarOuFalhar(Long id) {
        return jogadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", id));
    }
}
