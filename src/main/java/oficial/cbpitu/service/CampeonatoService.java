package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.exception.OperacaoInvalidaException;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.exception.RegraNegocioException;
import oficial.cbpitu.model.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;
import oficial.cbpitu.model.enums.StatusCampeonato;
import oficial.cbpitu.model.enums.StatusPartida;
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
        // Verificação para evitar duplicação (ex: clique duplo ou reprocessamento)
        long partidasExistentes = partidaRepository.countByFaseId(fase.getId());
        if (partidasExistentes > 0) {
            System.out.println("Confrontos já gerados para a fase " + fase.getId() + ". Pulando geração.");
            return;
        }

        GeradorDeConfrontos strategy = getStrategy(fase.getFormato());

        if (!strategy.validarNumeroTimes(times.size())) {
            throw new RegraNegocioException(strategy.getMensagemValidacao(times.size()));
        }

        List<Partida> partidas;

        // Se for fase de grupos, cria e salva os grupos primeiro
        if (fase.isGrupos() && strategy instanceof FaseDeGruposStrategy gruposStrategy) {
            // Verifica se grupos já existem (redundância necessária pois grupos sem
            // partidas são raros mas possíveis)
            if (fase.getGrupos() != null && !fase.getGrupos().isEmpty()) {
                // Usa grupos existentes? Ou limpa?
                // Melhor assumir que se existem grupos, a fase já foi iniciada.
                return;
            }

            List<Grupo> grupos = gruposStrategy.dividirEmGrupos(times, fase);
            grupoRepository.saveAll(grupos);

            // Gera partidas para os grupos persistidos
            partidas = gruposStrategy.gerarPartidasParaGrupos(grupos, fase);
        } else {
            partidas = strategy.gerarConfrontos(times, fase);
        }

        partidaRepository.saveAll(partidas);
    }

    // Lógica de Avanço Automático

    @Transactional
    public void verificarEProcessarAvanco(Long faseId, Partida partidaRecente) {
        Fase fase = faseRepository.findById(faseId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fase", faseId));

        if (Boolean.TRUE.equals(fase.getFinalizada())) {
            return;
        }

        switch (fase.getFormato()) {
            case MATA_MATA:
            case MATA_MATA_MD3:
            case MATA_MATA_MD5:
                processarAvancoMataMata(fase, partidaRecente);
                break;
            case SISTEMA_SUICO:
                processarAvancoSuico(fase, partidaRecente);
                break;
            case LOSER_BRACKET:
                processarAvancoLoserBracket(fase, partidaRecente);
                break;
            default:
                break;
        }
    }

    private void processarAvancoMataMata(Fase fase, Partida partidaRecente) {
        int rodada = partidaRecente.getRodada();
        List<Partida> partidasDaRodada = partidaRepository.findByFaseIdAndRodada(fase.getId(), rodada);

        boolean todasFinalizadas = partidasDaRodada.stream()
                .allMatch(p -> p.getStatus() == StatusPartida.FINALIZADA || p.getStatus() == StatusPartida.WO);

        if (todasFinalizadas) {
            List<Time> vencedores = partidasDaRodada.stream()
                    .map(Partida::getVencedor)
                    .toList();

            if (!vencedores.isEmpty() && vencedores.size() >= 2) {
                List<Partida> proximas = mataMataStrategy.gerarProximaRodada(fase, vencedores);
                partidaRepository.saveAll(proximas);
            }
        }
    }

    private void processarAvancoSuico(Fase fase, Partida partidaRecente) {
        int rodada = partidaRecente.getRodada();
        System.out.println("Processing Avanco Suico. Rodada: " + rodada);

        List<Partida> partidasDaRodada = partidaRepository.findByFaseIdAndRodada(fase.getId(), rodada);
        System.out.println("Partidas encontradas na rodada: " + partidasDaRodada.size());

        boolean todasFinalizadas = partidasDaRodada.stream()
                .allMatch(p -> p.getStatus() == StatusPartida.FINALIZADA || p.getStatus() == StatusPartida.WO);

        System.out.println("Todas finalizadas? " + todasFinalizadas);

        if (todasFinalizadas) {
            int qtdTimes = fase.getCampeonato().getTimesParticipantes().size();
            System.out.println("Qtd times participantes: " + qtdTimes);

            int calculado = Math.min(qtdTimes - 1, 5); // Default
            int configurado = (fase.getRodadasTotais() != null) ? fase.getRodadasTotais() : 0;

            // Se configurado for muito baixo (ex: 1) ou nulo, usa o calculado
            int rodadasTotais = (configurado > 1) ? configurado : calculado;

            System.out.println("Rodadas Totais (Config: " + configurado + ", Final: " + rodadasTotais + ")");

            if (rodada < rodadasTotais) {
                System.out.println("Gerando proxima rodada...");
                // Busca todas as partidas da fase para garantir cálculo correto
                List<Partida> todasPartidasFase = partidaRepository.findByFaseId(fase.getId());
                try {
                    List<Partida> proximas = sistemaSuicoStrategy.gerarProximaRodada(fase, todasPartidasFase, rodada);
                    partidaRepository.saveAll(proximas);
                    System.out.println("Salvas " + proximas.size() + " novas partidas.");
                } catch (Exception e) {
                    System.err.println("Erro ao gerar rodada suico:");
                    e.printStackTrace();
                    throw e;
                }
            } else {
                System.out.println("Ultima rodada alcancada ou ultrapassada.");
            }
        }
    }

    private void processarAvancoLoserBracket(Fase fase, Partida partidaRecente) {
        int rodada = partidaRecente.getRodada();
        boolean isWinnersBox = rodada < 100;

        if (isWinnersBox) {
            // ---> FLUXO WINNERS BRACKET
            List<Partida> partidasWB = partidaRepository.findByFaseIdAndRodada(fase.getId(), rodada);
            boolean wbFinalizada = partidasWB.stream().allMatch(this::isFinalizada);

            if (wbFinalizada) {
                List<Time> vencedoresWB = partidasWB.stream().map(Partida::getVencedor).toList();
                List<Time> perdedoresWB = partidasWB.stream().map(Partida::getPerdedor).toList();

                // 1. Gera próxima rodada WB (se houver + de 2 times)
                if (vencedoresWB.size() >= 2) {
                    List<Partida> proxWB = loserBracketStrategy.gerarProximaRodadaWinners(fase, vencedoresWB, rodada);
                    partidaRepository.saveAll(proxWB);
                } else if (vencedoresWB.size() == 1) {
                    // Chegou na final da WB. O vencedor espera o vencedor da LB na Grand Finals.
                    // (Logica de GF pode ser checada aqui ou no fluxo LB)
                }

                // 2. DROP PARA LOSERS BRACKET
                // Mapeamento: WB R1 -> LB R1; WB R2 -> LB R2; WB R3 -> LB R4; WB R4 -> LB R6...
                int targetRodadaLB = (rodada == 1) ? 1 : (rodada - 1) * 2;

                // Verifica se precisa de winners do LB anterior
                boolean precisaDeLB = (rodada > 1);
                List<Time> vencedoresLBAnterior = new ArrayList<>();

                if (precisaDeLB) {
                    // LB R(target-1) // Ex: WB R2 cai em LB R2. Precisa de winners de LB R1
                    int rodadaLBAnterior = 100 + (targetRodadaLB - 1);
                    List<Partida> partidasLBAnt = partidaRepository.findByFaseIdAndRodada(fase.getId(),
                            rodadaLBAnterior);
                    if (partidasLBAnt.stream().allMatch(this::isFinalizada)) {
                        vencedoresLBAnterior = partidasLBAnt.stream().map(Partida::getVencedor).toList();
                    } else {
                        // LB ainda não acabou, não gera nada agora. O gatilho virá do LB.
                        return;
                    }
                }

                // Gera rodada LB
                List<Partida> proxLB = loserBracketStrategy.gerarRodadaLosers(fase, perdedoresWB, vencedoresLBAnterior,
                        targetRodadaLB);
                partidaRepository.saveAll(proxLB);
            }

        } else {
            // ---> FLUXO LOSERS BRACKET
            List<Partida> partidasLB = partidaRepository.findByFaseIdAndRodada(fase.getId(), rodada);
            boolean lbFinalizada = partidasLB.stream().allMatch(this::isFinalizada);

            if (lbFinalizada) {
                List<Time> vencedoresLB = partidasLB.stream().map(Partida::getVencedor).toList();
                int rodadaAtualLB = rodada - 100;
                int proximaRodadaLB = rodadaAtualLB + 1;

                // Verifica se a próxima rodada é uma "Drop Round" (par) ou "Advance Round"
                // (ímpar)
                // Na nossa lógica simplificada:
                // LB R1 (vem do WB R1) -> Prox é LB R2 (recebe WB R2)
                // LB R2 (mistura) -> Prox é LB R3 (só vencedores de LB R2)
                // LB R3 (só winners) -> Prox é LB R4 (recebe WB R3)

                boolean isProximaDropRound = (proximaRodadaLB % 2 == 0);

                if (isProximaDropRound) {
                    // Precisa esperar os perdedores da WB correspondente
                    // LB R2 precisa de WB R2. LB R4 precisa de WB R3.
                    int rodadaWBCorrespondente = (proximaRodadaLB / 2) + 1;

                    List<Partida> partidasWB = partidaRepository.findByFaseIdAndRodada(fase.getId(),
                            rodadaWBCorrespondente);
                    if (partidasWB.stream().allMatch(this::isFinalizada)) {
                        List<Time> perdedoresWB = partidasWB.stream().map(Partida::getPerdedor).toList();
                        List<Partida> proxLB = loserBracketStrategy.gerarRodadaLosers(fase, perdedoresWB, vencedoresLB,
                                proximaRodadaLB);
                        partidaRepository.saveAll(proxLB);
                    }
                    // Se WB não acabou, espera.

                } else {
                    // Próxima é ímpar (ex: LB R3). Só vencedores daqui avançam entre si.
                    // Ou se for a final da LB...
                    // TODO: Verificar se é final da LB

                    List<Partida> proxLB = loserBracketStrategy.gerarRodadaLosers(fase, null, vencedoresLB,
                            proximaRodadaLB);
                    partidaRepository.saveAll(proxLB);
                }
            }
        }
    }

    private boolean isFinalizada(Partida p) {
        return p.getStatus() == StatusPartida.FINALIZADA || p.getStatus() == StatusPartida.WO;
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

        System.out.println("Avancar Fase: Total Partidas=" + total + ", Finalizadas=" + finalizadas);

        if (!total.equals(finalizadas)) {
            System.err.println("Impossível avançar: Partidas pendentes.");
            throw new OperacaoInvalidaException(
                    String.format("Ainda há %d partidas pendentes nesta fase", total - finalizadas));
        }

        // Calcula classificados
        GeradorDeConfrontos strategy = getStrategy(faseAtual.getFormato());
        List<Time> classificados = strategy.calcularClassificados(faseAtual);
        System.out.println("Classificados calculados: " + classificados.size());

        // Marca fase como finalizada
        faseAtual.setFinalizada(true);
        faseRepository.save(faseAtual);

        // Verifica se há próxima fase
        int proximaOrdem = faseAtual.getOrdem() + 1;
        Optional<Fase> proximaFaseOpt = faseRepository
                .findByCampeonatoIdAndOrdem(campeonatoId, proximaOrdem);

        if (proximaFaseOpt.isPresent()) {
            Fase proximaFase = proximaFaseOpt.get();
            System.out.println("Gerando confrontos para próxima fase: " + proximaFase.getNome());
            gerarConfrontosDaFase(proximaFase, classificados);
        } else {
            System.out.println("Sem próxima fase. Finalizando campeonato.");
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
