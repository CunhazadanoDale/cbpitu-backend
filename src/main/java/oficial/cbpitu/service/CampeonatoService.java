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
    private final JogadorRepository jogadorRepository;
    private final EscalacaoRepository escalacaoRepository;

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

        if (campeonato.getEdicao() != null) {
            // Verifica se já existe escalação para este time nesta edição
            if (!escalacaoRepository.existsByTimeIdAndEdicaoId(time.getId(), campeonato.getEdicao().getId())) {
                Escalacao novaEscalacao = new Escalacao();
                novaEscalacao.setTime(time);
                novaEscalacao.setEdicao(campeonato.getEdicao());
                novaEscalacao.setCapitao(time.getCapitao());
                // Copia os jogadores do time para a escalação da edição
                novaEscalacao.setJogadores(new java.util.HashSet<>(time.getJogadores()));
                
                escalacaoRepository.save(novaEscalacao);
                System.out.println("Escalação criada automaticamente para o time " + time.getNomeTime() 
                        + " na edição " + campeonato.getEdicao().getNome());
            }
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
            // Verifica se grupos já existem
            if (fase.getGrupos() != null && !fase.getGrupos().isEmpty()) {
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

    @Transactional
    public void gerarConfrontosManuais(Long faseId, List<oficial.cbpitu.dto.ParConfrontoDTO> confrontos) {
        Fase fase = faseRepository.findById(faseId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fase", faseId));

        if (Boolean.TRUE.equals(fase.getFinalizada())) {
            throw new OperacaoInvalidaException("Fase já está finalizada");
        }

        long partidasExistentes = partidaRepository.countByFaseId(fase.getId());
        if (partidasExistentes > 0) {
            throw new OperacaoInvalidaException("Confrontos já foram gerados para esta fase");
        }

        List<Partida> partidas = new ArrayList<>();
        int rodada = 1;
        int numPartidas = confrontos.size();
        
        // Determina nome da fase (Final, Semis, etc)
        // Lógica simplificada baseada no mata-mata strategy
        String nomeFase = "RODADA DE " + (numPartidas * 2);
        if (numPartidas == 1) nomeFase = "FINAL";
        else if (numPartidas == 2) nomeFase = "SEMIFINAL";
        else if (numPartidas == 4) nomeFase = "QUARTAS";
        else if (numPartidas == 8) nomeFase = "OITAVAS";

        for (int i = 0; i < confrontos.size(); i++) {
            oficial.cbpitu.dto.ParConfrontoDTO par = confrontos.get(i);
            
            Time time1 = timeRepository.findById(par.getTime1Id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Time", par.getTime1Id()));
            Time time2 = timeRepository.findById(par.getTime2Id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Time", par.getTime2Id()));

            Partida partida = new Partida();
            partida.setTime1(time1);
            partida.setTime2(time2);
            partida.setFase(fase);
            partida.setRodada(rodada);
            partida.setStatus(StatusPartida.PENDENTE);
            partida.setIdentificadorBracket(nomeFase + " " + (i + 1));
            
            partidas.add(partida);
        }

        partidaRepository.saveAll(partidas);
        
        // Atualiza status do campeonato se necessário
        Campeonato campeonato = fase.getCampeonato();
        if (campeonato.getStatus() == StatusCampeonato.INSCRICOES_ENCERRADAS || 
            campeonato.getStatus() == StatusCampeonato.INSCRICOES_ABERTAS) {
            campeonato.setStatus(StatusCampeonato.EM_ANDAMENTO);
            campeonatoRepository.save(campeonato);
        }
    }

    @Transactional
    public void gerarGruposManuais(Long faseId, List<oficial.cbpitu.dto.GrupoManualDTO> gruposManuais) {
        Fase fase = faseRepository.findById(faseId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fase", faseId));

        if (Boolean.TRUE.equals(fase.getFinalizada())) {
            throw new OperacaoInvalidaException("Fase já está finalizada");
        }
        
        // Verifica se é realmente uma fase de grupos
        if (!fase.isGrupos()) {
             throw new OperacaoInvalidaException("Esta fase não é de grupos");
        }

        long partidasExistentes = partidaRepository.countByFaseId(fase.getId());
        if (partidasExistentes > 0) {
            throw new OperacaoInvalidaException("Partidas já foram geradas para esta fase");
        }
        
        // Verifica se grupos já existem
        if (fase.getGrupos() != null && !fase.getGrupos().isEmpty()) {
             // Limpa grupos existentes se não houver partidas (caso raro de retry)
             grupoRepository.deleteAll(fase.getGrupos());
             fase.getGrupos().clear();
        }

        List<Grupo> gruposSalvos = new ArrayList<>();
        
        for (oficial.cbpitu.dto.GrupoManualDTO dto : gruposManuais) {
            Grupo grupo = new Grupo();
            grupo.setNome(dto.getNome());
            grupo.setFase(fase);
            
            for (Long timeId : dto.getTimesIds()) {
                Time time = timeRepository.findById(timeId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Time", timeId));
                grupo.adicionarTime(time);
            }
            
            gruposSalvos.add(grupoRepository.save(grupo));
        }
        
        // Gera partidas usando a strategy existente
        if (getStrategy(fase.getFormato()) instanceof FaseDeGruposStrategy gruposStrategy) {
            List<Partida> partidas = gruposStrategy.gerarPartidasParaGrupos(gruposSalvos, fase);
            partidaRepository.saveAll(partidas);
        }
        
        // Atualiza status do campeonato
        Campeonato campeonato = fase.getCampeonato();
        if (campeonato.getStatus() == StatusCampeonato.INSCRICOES_ENCERRADAS || 
            campeonato.getStatus() == StatusCampeonato.INSCRICOES_ABERTAS) {
            campeonato.setStatus(StatusCampeonato.EM_ANDAMENTO);
            campeonatoRepository.save(campeonato);
        }
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
        String identificador = partidaRecente.getIdentificadorBracket();
        
        System.out.println("=== processarAvancoLoserBracket ===");
        System.out.println("Rodada: " + rodada + ", Identificador: " + identificador);

        // GRAND FINALS
        if (rodada == 0 || (identificador != null && identificador.startsWith("GF"))) {
            // Check if GF-RESET is needed
            if (identificador != null && identificador.equals("GF") && partidaRecente.isFinalizada()) {
                Time vencedor = partidaRecente.getVencedor();
                Time vencedorWB = partidaRecente.getTime1(); // WB winner is always time1
                
                if (!vencedor.equals(vencedorWB)) {
                    // LB winner beat WB winner - need reset!
                    System.out.println("LB winner ganhou GF - Gerando RESET");
                    Partida reset = loserBracketStrategy.gerarGrandFinalsReset(fase, 
                            partidaRecente.getTime1(), partidaRecente.getTime2());
                    partidaRepository.save(reset);
                }
            }
            return;
        }

        // LOSERS BRACKET (rodada >= 100)
        if (rodada >= 100) {
            processarAvancoLB(fase, partidaRecente);
            return;
        }

        // WINNERS BRACKET (rodada < 100)
        processarAvancoWB(fase, partidaRecente);
    }

    private void processarAvancoWB(Fase fase, Partida partidaRecente) {
        int rodada = partidaRecente.getRodada();
        List<Partida> partidasWB = partidaRepository.findByFaseIdAndRodada(fase.getId(), rodada);
        
        if (!partidasWB.stream().allMatch(this::isFinalizada)) {
            System.out.println("WB Rodada " + rodada + " ainda não finalizada");
            return;
        }

        List<Time> vencedoresWB = partidasWB.stream().map(Partida::getVencedor).toList();
        List<Time> perdedoresWB = partidasWB.stream().map(Partida::getPerdedor).toList();
        
        System.out.println("WB R" + rodada + " finalizada. Vencedores: " + vencedoresWB.size() + ", Perdedores: " + perdedoresWB.size());

        // Calcula número total de rodadas WB baseado no tamanho inicial
        int numTimesTotal = calcularNumTimesInicial(fase);
        int totalRodadasWB = calcularTotalRodadasWB(numTimesTotal);
        int rodadaAtualWB = totalRodadasWB - rodada + 1; // Converte para 1-indexed

        System.out.println("Total times: " + numTimesTotal + ", Total rodadas WB: " + totalRodadasWB);
        System.out.println("Rodada atual WB (1-indexed): " + rodadaAtualWB);

        boolean isWBFinal = (rodada == 1);

        // 1. Avança vencedores no WB (se não for final)
        if (!isWBFinal && vencedoresWB.size() >= 2) {
            List<Partida> proxWB = loserBracketStrategy.gerarProximaRodadaWinners(fase, vencedoresWB, rodada);
            partidaRepository.saveAll(proxWB);
            System.out.println("Geradas " + proxWB.size() + " partidas para WB R" + (rodada - 1));
        }

        // 2. Processa perdedores para o LB
        if (rodadaAtualWB == 1) {
            // Primeira rodada WB - perdedores vão para LB R1
            // Verifica se LB R1 já existe
            List<Partida> lbR1Existente = partidaRepository.findByFaseIdAndRodada(fase.getId(), 101);
            if (lbR1Existente.isEmpty()) {
                List<Partida> lbR1 = loserBracketStrategy.gerarRodadaLosers(fase, null, perdedoresWB, 1);
                partidaRepository.saveAll(lbR1);
                System.out.println("Geradas " + lbR1.size() + " partidas para LB R1");
            }
        } else if (isWBFinal) {
            // WB Final - perdedor vai para LB Final enfrentar vencedor do LB
            Time perdedorWBFinal = perdedoresWB.get(0);
            Time vencedorWB = vencedoresWB.get(0);
            
            // Calcula qual é a última rodada do LB
            int lbFinalRodada = calcularLBFinalRodada(numTimesTotal);
            
            // Verifica se o LB está pronto (vencedor do LB)
            List<Partida> partidasLBAnterior = partidaRepository.findByFaseIdAndRodada(fase.getId(), 100 + lbFinalRodada - 1);
            
            if (!partidasLBAnterior.isEmpty() && partidasLBAnterior.stream().allMatch(this::isFinalizada)) {
                Time vencedorLB = partidasLBAnterior.get(0).getVencedor();
                
                // Gera LB Final
                List<Partida> lbFinal = loserBracketStrategy.gerarRodadaLosers(fase, 
                        List.of(perdedorWBFinal), List.of(vencedorLB), lbFinalRodada);
                partidaRepository.saveAll(lbFinal);
                System.out.println("Gerada LB Final (R" + lbFinalRodada + ")");
            } else {
                // LB ainda não está pronto - o gatilho virá do LB
                System.out.println("LB ainda não finalizado, aguardando...");
            }
            
            // Guarda o vencedor WB para Grand Finals (será resgatado depois)
        } else {
            // Rodadas intermediárias WB - perdedores caem para rodada LB correspondente
            // Para 8 times: WB R2 perdedores -> LB R2 (enfrentam winners LB R1)
            int targetLBRodada = rodadaAtualWB; // Simplificado para este caso
            
            // Verifica se LB anterior está finalizado
            List<Partida> lbAnterior = partidaRepository.findByFaseIdAndRodada(fase.getId(), 100 + targetLBRodada - 1);
            
            if (!lbAnterior.isEmpty() && lbAnterior.stream().allMatch(this::isFinalizada)) {
                List<Time> vencedoresLB = lbAnterior.stream().map(Partida::getVencedor).toList();
                List<Partida> proxLB = loserBracketStrategy.gerarRodadaLosers(fase, perdedoresWB, vencedoresLB, targetLBRodada);
                partidaRepository.saveAll(proxLB);
                System.out.println("Geradas " + proxLB.size() + " partidas para LB R" + targetLBRodada);
            }
        }
    }

    private void processarAvancoLB(Fase fase, Partida partidaRecente) {
        int rodada = partidaRecente.getRodada();
        int rodadaLB = rodada - 100;
        
        List<Partida> partidasLB = partidaRepository.findByFaseIdAndRodada(fase.getId(), rodada);
        
        if (!partidasLB.stream().allMatch(this::isFinalizada)) {
            System.out.println("LB Rodada " + rodadaLB + " ainda não finalizada");
            return;
        }

        List<Time> vencedoresLB = partidasLB.stream().map(Partida::getVencedor).toList();
        System.out.println("LB R" + rodadaLB + " finalizada. Vencedores: " + vencedoresLB.size());

        int numTimesTotal = calcularNumTimesInicial(fase);
        int lbFinalRodada = calcularLBFinalRodada(numTimesTotal);

        // Verifica se é a LB Final
        if (rodadaLB == lbFinalRodada) {
            // LB Final acabou - gera Grand Finals
            Time vencedorLB = vencedoresLB.get(0);
            
            // Busca vencedor do WB Final
            List<Partida> wbFinal = partidaRepository.findByFaseIdAndRodada(fase.getId(), 1);
            if (!wbFinal.isEmpty() && wbFinal.get(0).isFinalizada()) {
                Time vencedorWB = wbFinal.get(0).getVencedor();
                
                // Verifica se GF já existe
                List<Partida> gfExistentes = partidaRepository.findByFaseIdAndRodada(fase.getId(), 0);
                if (gfExistentes.isEmpty()) {
                    Partida grandFinals = loserBracketStrategy.gerarGrandFinals(fase, vencedorWB, vencedorLB);
                    partidaRepository.save(grandFinals);
                    System.out.println("Gerada Grand Finals!");
                }
            }
            return;
        }

        // Verifica se precisa aguardar perdedores do WB
        int proximaLBRodada = rodadaLB + 1;
        
        // Para 4 times: LB R1 -> LB R2 (Final) que precisa do perdedor WB Final
        // Para 8 times: LB R1 -> LB R2 (precisa perdedores WB R2)
        
        if (proximaLBRodada == lbFinalRodada) {
            // Próxima é a LB Final - precisa do perdedor da WB Final
            List<Partida> wbFinal = partidaRepository.findByFaseIdAndRodada(fase.getId(), 1);
            
            if (!wbFinal.isEmpty() && wbFinal.stream().allMatch(this::isFinalizada)) {
                List<Time> perdedoresWBFinal = wbFinal.stream().map(Partida::getPerdedor).toList();
                List<Partida> lbFinal = loserBracketStrategy.gerarRodadaLosers(fase, 
                        perdedoresWBFinal, vencedoresLB, proximaLBRodada);
                partidaRepository.saveAll(lbFinal);
                System.out.println("Gerada LB Final (R" + proximaLBRodada + ")");
            } else {
                System.out.println("Aguardando WB Final terminar...");
            }
        } else {
            // Rodada intermediária - verifica se há perdedores WB para misturar
            // Para 8 times, LB R2 recebe perdedores de WB R2
            int wbRodadaCorrespondente = calcularWBRodadaParaLB(numTimesTotal, proximaLBRodada);
            
            if (wbRodadaCorrespondente > 0) {
                List<Partida> wbCorrespondente = partidaRepository.findByFaseIdAndRodada(fase.getId(), wbRodadaCorrespondente);
                
                if (!wbCorrespondente.isEmpty() && wbCorrespondente.stream().allMatch(this::isFinalizada)) {
                    List<Time> perdedoresWB = wbCorrespondente.stream().map(Partida::getPerdedor).toList();
                    List<Partida> proxLB = loserBracketStrategy.gerarRodadaLosers(fase, 
                            perdedoresWB, vencedoresLB, proximaLBRodada);
                    partidaRepository.saveAll(proxLB);
                    System.out.println("Geradas " + proxLB.size() + " partidas para LB R" + proximaLBRodada);
                }
            } else {
                // Só vencedores LB avançam entre si
                if (vencedoresLB.size() >= 2) {
                    List<Partida> proxLB = loserBracketStrategy.gerarRodadaLosers(fase, 
                            null, vencedoresLB, proximaLBRodada);
                    partidaRepository.saveAll(proxLB);
                    System.out.println("Geradas " + proxLB.size() + " partidas para LB R" + proximaLBRodada);
                }
            }
        }
    }

    // Helpers para Double Elimination
    private int calcularNumTimesInicial(Fase fase) {
        // Pega da contagem de times do campeonato
        return fase.getCampeonato().getTimesParticipantes().size();
    }

    private int calcularTotalRodadasWB(int numTimes) {
        // 4 times = 2 rodadas WB (R2, R1=Final)
        // 8 times = 3 rodadas WB (R3, R2, R1=Final)
        return (int) (Math.log(numTimes) / Math.log(2));
    }

    private int calcularLBFinalRodada(int numTimes) {
        // 4 times: LB Final = R2
        // 8 times: LB Final = R4
        // Fórmula: 2 * (rodadas WB - 1)
        int rodadasWB = calcularTotalRodadasWB(numTimes);
        return Math.max(2, 2 * (rodadasWB - 1));
    }

    private int calcularWBRodadaParaLB(int numTimes, int lbRodada) {
        // Retorna qual rodada WB alimenta perdedores para a LB rodada especificada
        // Para 8 times:
        //   LB R1 <- WB R3 perdedores (primeira rodada)
        //   LB R2 <- WB R2 perdedores + LB R1 winners
        //   LB R3 <- só LB R2 winners (rodada de redução)
        //   LB R4 <- WB R1 (final) perdedor + LB R3 winner
        
        int rodadasWB = calcularTotalRodadasWB(numTimes);
        
        // Para 4 times (2 rodadas WB):
        //   LB R1 <- WB R2 perdedores
        //   LB R2 <- WB R1 perdedor + LB R1 winner
        if (numTimes == 4) {
            if (lbRodada == 1) return 2; // WB R2 -> LB R1
            if (lbRodada == 2) return 1; // WB Final -> LB Final
        }
        
        // Para 8 times (3 rodadas WB):
        if (numTimes == 8) {
            if (lbRodada == 1) return 3; // WB R3 -> LB R1
            if (lbRodada == 2) return 2; // WB R2 -> LB R2
            if (lbRodada == 3) return 0; // Só LB winners
            if (lbRodada == 4) return 1; // WB Final -> LB Final
        }
        
        return 0;
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
                Time campeao = classificados.get(0);
                campeonato.setCampeao(campeao);
                
                // Atribui títulos ao time e jogadores
                atribuirTitulos(campeonato, campeao);
            }
            campeonato.setStatus(StatusCampeonato.FINALIZADO);
        }

        return campeonatoRepository.save(campeonato);
    }
    
    /**
     * Atribui títulos ao time campeão e seus jogadores.
     * Usa a escalação da edição se existir, senão usa os jogadores atuais do time.
     */
    private void atribuirTitulos(Campeonato campeonato, Time campeao) {
        System.out.println("Atribuindo títulos para o campeão: " + campeao.getNomeTime());
        
        // Incrementa troféu do time
        Integer trofeusAtuais = campeao.getTrofeus() != null ? campeao.getTrofeus() : 0;
        campeao.setTrofeus(trofeusAtuais + 1);
        timeRepository.save(campeao);
        System.out.println("Time agora tem " + campeao.getTrofeus() + " troféu(s)");
        
        // Busca jogadores que devem receber o título
        java.util.Set<Jogador> jogadoresParaPremiar = new java.util.HashSet<>();
        
        // Se campeonato tem edição, busca escalação daquela edição
        if (campeonato.getEdicao() != null) {
            Optional<Escalacao> escalacaoOpt = escalacaoRepository
                    .findByTimeIdAndEdicaoId(campeao.getId(), campeonato.getEdicao().getId());
            
            if (escalacaoOpt.isPresent()) {
                jogadoresParaPremiar.addAll(escalacaoOpt.get().getJogadores());
                System.out.println("Usando escalação da edição: " + jogadoresParaPremiar.size() + " jogadores");
            }
        }
        
        // Se não encontrou escalação, usa jogadores atuais do time
        if (jogadoresParaPremiar.isEmpty() && campeao.getJogadores() != null) {
            jogadoresParaPremiar.addAll(campeao.getJogadores());
            System.out.println("Usando roster atual do time: " + jogadoresParaPremiar.size() + " jogadores");
        }
        
        // Incrementa título de cada jogador
        for (Jogador jogador : jogadoresParaPremiar) {
            Integer titulosAtuais = jogador.getTitulos() != null ? jogador.getTitulos() : 0;
            jogador.setTitulos(titulosAtuais + 1);
            jogadorRepository.save(jogador);
            System.out.println("Jogador " + jogador.getNickname() + " agora tem " + jogador.getTitulos() + " título(s)");
        }
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
