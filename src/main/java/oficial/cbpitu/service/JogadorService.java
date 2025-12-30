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
    private final oficial.cbpitu.repository.EscalacaoRepository escalacaoRepository; // [NEW]

    public oficial.cbpitu.dto.JogadorDetalhesDTO buscarDetalhes(Long id) {
        Jogador jogador = buscarOuFalhar(id);
        
        // Busca histórico de escalações
        List<oficial.cbpitu.model.Escalacao> historico = escalacaoRepository.findByJogadoresId(id);
        
        List<oficial.cbpitu.dto.JogadorDetalhesDTO.HistoricoItemDTO> historicoDTO = historico.stream()
            .sorted((e1, e2) -> {
                int anoCompare = e2.getEdicao().getAno().compareTo(e1.getEdicao().getAno());
                if (anoCompare != 0) return anoCompare;
                return e2.getEdicao().getId().compareTo(e1.getEdicao().getId());
            })
            .map(e -> oficial.cbpitu.dto.JogadorDetalhesDTO.HistoricoItemDTO.builder()
                .nomeTime(e.getTime().getNomeTime())
                .edicaoId(e.getEdicao().getId())
                .nomeEdicao(e.getEdicao().getNomeCompleto())
                .anoEdicao(e.getEdicao().getAno())
                .isCapitao(e.getCapitao() != null && e.getCapitao().getId().equals(id))
                .build())
            .toList();
            
        return oficial.cbpitu.dto.JogadorDetalhesDTO.builder()
            .id(jogador.getId())
            .nickname(jogador.getNickname())
            .nomeReal(jogador.getNomeReal())
            .laneLol(jogador.getLaneLol())
            .titulos(jogador.getTitulos())
            .historico(historicoDTO)
            .build();
    }

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

        // 1. Remove jogador de todos os times atuais
        for (Time time : jogador.getTimes()) {
            time.getJogadores().remove(jogador);
            if (time.getCapitao() != null && time.getCapitao().equals(jogador)) {
                time.setCapitao(null);
            }
            // Importante: salvar o time para persistir a remoção na tabela de join
            // (Embora JPA possa fazer automaticamente, é mais garantido)
            // Precisaria do TimeRepository aqui? Ou Dirty Checking resolve.
            // Pelo Dirty Checking deve resolver pois 'time' é entidade gerenciada via jogador.getTimes()
        }
        
        // 2. Remove jogador de históricos de escalação
        List<oficial.cbpitu.model.Escalacao> escalacoes = escalacaoRepository.findByJogadoresId(id);
        for (oficial.cbpitu.model.Escalacao esc : escalacoes) {
            esc.removerJogador(jogador);
            if (esc.getCapitao() != null && esc.getCapitao().equals(jogador)) {
                esc.setCapitao(null);
            }
            escalacaoRepository.save(esc);
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
