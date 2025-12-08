package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.exception.OperacaoInvalidaException;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.exception.RegraNegocioException;
import oficial.cbpitu.model.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;
import oficial.cbpitu.model.enums.StatusCampeonato;
import oficial.cbpitu.repository.*;
import oficial.cbpitu.service.strategy.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampeonatoService {

    private final CampeonatoRepository campeonatoRepository;
    private final FaseRepository faseRepository;
    private final GrupoRepository grupoRepository;
    private final PartidaRepository partidaRepository;
    private final TimeRepository timeRepository;

    // Strategies
    private final MataMataStrategy mataMataStrategy;
    private final FaseDeGruposStrategy faseDeGruposStrategy;
    private final SistemaSuicoStrategy sistemaSuicoStrategy;
    private final LoserBracketStrategy loserBracketStrategy;

    // CRUD Campeonato

    public List<Campeonato> listarTodos() {
        return campeonatoRepository.findAll();
    }

    public Optional<Campeonato> buscarPorId(Long id) {
        return campeonatoRepository.findById(id);
    }

    public List<Campeonato> listarAtivos() {
        return campeonatoRepository.findCampeonatosAtivos();
    }

    @Transactional
    public Campeonato criar(Campeonato campeonato) {
        campeonato.setStatus(StatusCampeonato.RASCUNHO);
        return campeonatoRepository.save(campeonato);
    }

    @Transactional
    public Campeonato atualizar(Long id, Campeonato dados) {
        Campeonato campeonato = buscarOuFalhar(id);

        campeonato.setNome(dados.getNome());
        campeonato.setDescricao(dados.getDescricao());
        campeonato.setDataInicio(dados.getDataInicio());
        campeonato.setDataFim(dados.getDataFim());
        campeonato.setLimiteMaximoTimes(dados.getLimiteMaximoTimes());

        return campeonatoRepository.save(campeonato);
    }

    @Transactional
    public void deletar(Long id) {
        Campeonato campeonato = buscarOuFalhar(id);
        campeonatoRepository.delete(campeonato);
    }

    // Inscrição de Times

    @Transactional
    public Campeonato abrirInscricoes(Long campeonatoId) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);
        campeonato.setStatus(StatusCampeonato.INSCRICOES_ABERTAS);
        return campeonatoRepository.save(campeonato);
    }

    @Transactional
    public Campeonato fecharInscricoes(Long campeonatoId) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);
        campeonato.setStatus(StatusCampeonato.INSCRICOES_ENCERRADAS);
        return campeonatoRepository.save(campeonato);
    }

    @Transactional
    public Campeonato inscreverTime(Long campeonatoId, Long timeId) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);
        Time time = timeRepository.findById(timeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Time", timeId));

        if (!campeonato.podeInscreverTime()) {
            throw new OperacaoInvalidaException("Inscrições não estão abertas ou limite de times atingido");
        }

        if (campeonato.getTimesParticipantes().contains(time)) {
            throw new RegraNegocioException("Time já está inscrito neste campeonato");
        }

        campeonato.adicionarTime(time);
        return campeonatoRepository.save(campeonato);
    }

    @Transactional
    public Campeonato removerTime(Long campeonatoId, Long timeId) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);
        Time time = timeRepository.findById(timeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Time", timeId));

        if (!campeonato.getTimesParticipantes().contains(time)) {
            throw new RegraNegocioException("Time não está inscrito neste campeonato");
        }

        campeonato.removerTime(time);
        return campeonatoRepository.save(campeonato);
    }

    // Gerenciamento de Fases

    @Transactional
    public Fase adicionarFase(Long campeonatoId, String nome, FormatoCompeticao formato,
            Integer classificados, Integer rodadas) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);

        Fase fase = new Fase();
        fase.setNome(nome);
        fase.setFormato(formato);
        fase.setOrdem(campeonato.getFases().size() + 1);
        fase.setClassificadosNecessarios(classificados);
        fase.setRodadasTotais(rodadas);
        fase.setFinalizada(false);

        campeonato.adicionarFase(fase);
        campeonatoRepository.save(campeonato);

        return fase;
    }

    public String validarConfiguracao(Long campeonatoId, FormatoCompeticao formato) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);
        int numTimes = campeonato.getNumeroTimesInscritos();

        GeradorDeConfrontos strategy = getStrategy(formato);
        return strategy.getMensagemValidacao(numTimes);
    }

    // Iniciar Campeonato

    @Transactional
    public Campeonato iniciarCampeonato(Long campeonatoId) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);

        if (campeonato.getFases().isEmpty()) {
            throw new RegraNegocioException("Campeonato precisa ter pelo menos uma fase configurada");
        }

        if (campeonato.getNumeroTimesInscritos() < 2) {
            throw new RegraNegocioException("Campeonato precisa ter pelo menos 2 times inscritos");
        }

        // Gera confrontos da primeira fase
        Fase primeiraFase = campeonato.getFases().get(0);
        gerarConfrontosDaFase(primeiraFase, new ArrayList<>(campeonato.getTimesParticipantes()));

        campeonato.setStatus(StatusCampeonato.EM_ANDAMENTO);
        return campeonatoRepository.save(campeonato);
    }

    @Transactional
    public void gerarConfrontosDaFase(Fase fase, List<Time> times) {
        GeradorDeConfrontos strategy = getStrategy(fase.getFormato());

        if (!strategy.validarNumeroTimes(times.size())) {
            throw new RegraNegocioException(strategy.getMensagemValidacao(times.size()));
        }

        List<Partida> partidas = strategy.gerarConfrontos(times, fase);

        // Se for fase de grupos, salva os grupos
        if (fase.isGrupos() && strategy instanceof FaseDeGruposStrategy gruposStrategy) {
            List<Grupo> grupos = gruposStrategy.dividirEmGrupos(times, fase);
            for (Grupo grupo : grupos) {
                grupoRepository.save(grupo);
            }
        }

        for (Partida partida : partidas) {
            partidaRepository.save(partida);
        }
    }

    // Avançar Fase

    @Transactional
    public Campeonato avancarParaProximaFase(Long campeonatoId) {
        Campeonato campeonato = buscarOuFalhar(campeonatoId);

        // Encontra fase atual em andamento
        Fase faseAtual = faseRepository
                .findFirstByCampeonatoIdAndFinalizadaFalseOrderByOrdemAsc(campeonatoId)
                .orElseThrow(() -> new OperacaoInvalidaException("Nenhuma fase em andamento"));

        // Verifica se todas as partidas foram finalizadas
        Long total = partidaRepository.countByFaseId(faseAtual.getId());
        Long finalizadas = partidaRepository.countFinalizadasByFaseId(faseAtual.getId());

        if (!total.equals(finalizadas)) {
            throw new OperacaoInvalidaException(
                    String.format("Ainda há %d partidas pendentes nesta fase", total - finalizadas));
        }

        // Calcula classificados
        GeradorDeConfrontos strategy = getStrategy(faseAtual.getFormato());
        List<Time> classificados = strategy.calcularClassificados(faseAtual);

        // Marca fase como finalizada
        faseAtual.setFinalizada(true);
        faseRepository.save(faseAtual);

        // Verifica se há próxima fase
        int proximaOrdem = faseAtual.getOrdem() + 1;
        Optional<Fase> proximaFaseOpt = faseRepository
                .findByCampeonatoIdAndOrdem(campeonatoId, proximaOrdem);

        if (proximaFaseOpt.isPresent()) {
            Fase proximaFase = proximaFaseOpt.get();
            gerarConfrontosDaFase(proximaFase, classificados);
        } else {
            // Campeonato finalizado - determina campeão
            if (!classificados.isEmpty()) {
                campeonato.setCampeao(classificados.get(0));
            }
            campeonato.setStatus(StatusCampeonato.FINALIZADO);
        }

        return campeonatoRepository.save(campeonato);
    }

    // Helpers

    private Campeonato buscarOuFalhar(Long id) {
        return campeonatoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Campeonato", id));
    }

    private GeradorDeConfrontos getStrategy(FormatoCompeticao formato) {
        return switch (formato) {
            case MATA_MATA, MATA_MATA_MD3, MATA_MATA_MD5 -> mataMataStrategy;
            case GRUPOS, GRUPOS_IDA_VOLTA -> faseDeGruposStrategy;
            case SISTEMA_SUICO -> sistemaSuicoStrategy;
            case LOSER_BRACKET -> loserBracketStrategy;
        };
    }
}
