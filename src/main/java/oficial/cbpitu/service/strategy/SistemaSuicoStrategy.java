package oficial.cbpitu.service.strategy;

import oficial.cbpitu.model.Fase;
import oficial.cbpitu.model.Partida;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.model.enums.StatusPartida;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy para Sistema Suíço.
 * Pareia times com pontuação similar e evita repetir confrontos.
 */
@Component
public class SistemaSuicoStrategy implements GeradorDeConfrontos {

    private static final int RODADAS_PADRAO = 5;

    @Override
    public List<Partida> gerarConfrontos(List<Time> times, Fase fase) {
        // Sistema suíço gera apenas a primeira rodada inicialmente
        // As próximas rodadas são geradas após resultados
        return gerarPrimeiraRodada(times, fase);
    }

    /**
     * Gera a primeira rodada (aleatória).
     */
    private List<Partida> gerarPrimeiraRodada(List<Time> times, Fase fase) {
        List<Partida> partidas = new ArrayList<>();
        List<Time> timesSorteados = new ArrayList<>(times);
        Collections.shuffle(timesSorteados);

        for (int i = 0; i < timesSorteados.size() - 1; i += 2) {
            Partida partida = new Partida();
            partida.setTime1(timesSorteados.get(i));
            partida.setTime2(timesSorteados.get(i + 1));
            partida.setFase(fase);
            partida.setRodada(1);
            partida.setStatus(StatusPartida.PENDENTE);
            partidas.add(partida);
        }

        // Se número ímpar, último time fica de folga (bye)
        return partidas;
    }

    /**
     * Gera próxima rodada baseada em pontuações.
     * Utiliza IDs para garantir consistência e evita problemas com proxis do
     * Hibernate.
     */
    public List<Partida> gerarProximaRodada(Fase fase, List<Partida> todasPartidas, int rodadaAtual) {
        List<Partida> novasPartidas = new ArrayList<>();

        // 1. Mapa de Pontuações por ID
        Map<Long, Integer> pontuacoes = calcularPontuacoes(todasPartidas);

        // 2. Cache de objetos Time (para criar as partidas depois)
        Map<Long, Time> timeCache = new HashMap<>();
        // Populando cache com times das partidas
        todasPartidas.forEach(p -> {
            if (p.getTime1() != null)
                timeCache.put(p.getTime1().getId(), p.getTime1());
            if (p.getTime2() != null)
                timeCache.put(p.getTime2().getId(), p.getTime2());
        });
        // Populando cache com participantes do campeonato (caso ainda não tenham jogado
        // ou para garantir todos)
        for (Time t : fase.getCampeonato().getTimesParticipantes()) {
            timeCache.put(t.getId(), t);
        }

        // 3. Verifica confrontos já realizados (Set de "ID1-ID2")
        Set<String> confrontosJaRealizados = getConfrontosRealizados(todasPartidas);

        // 4. Lista base de times (IDs)
        // Adiciona todos os participantes ao mapa de pontuação se não existirem
        for (Long timeId : timeCache.keySet()) {
            pontuacoes.putIfAbsent(timeId, 0);
        }

        // 5. Ordena times por pontuação
        List<Long> timesOrdenados = pontuacoes.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()) // Pontuação desc
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Set<Long> timesJaPareados = new HashSet<>();
        int proximaRodada = rodadaAtual + 1;

        for (Long timeId : timesOrdenados) {
            if (timesJaPareados.contains(timeId))
                continue;

            // Procura adversário
            Long adversarioId = encontrarAdversario(timeId, timesOrdenados, pontuacoes,
                    confrontosJaRealizados, timesJaPareados);

            if (adversarioId != null) {
                Partida partida = new Partida();
                partida.setTime1(timeCache.get(timeId));
                partida.setTime2(timeCache.get(adversarioId));
                partida.setFase(fase);
                partida.setRodada(proximaRodada);
                partida.setStatus(StatusPartida.PENDENTE);
                partida.setIdentificadorBracket("S-R" + proximaRodada + "-" + (novasPartidas.size() + 1));
                novasPartidas.add(partida);

                System.out.println("Gerada partida Suico: " + timeId + " vs " + adversarioId);

                timesJaPareados.add(timeId);
                timesJaPareados.add(adversarioId);
                confrontosJaRealizados.add(gerarChaveConfronto(timeId, adversarioId));
            } else {
                // BYE (Folga)
                // Se sobrou um time sem adversário válido (ex: ímpar ou incompatibilidade
                // total)
                // Na regra suíça, ele recebe um "Bye".
                // Geramos uma partida finalizada com vitória para ele.
                // Mas para não quebrar fluxo, vamos deixar pendente com Time2 null ou tratar
                // como vitória auto.
                // Simplificação: Partida Pendente com Time2 = null (Frontend deve tratar) ou
                // Finalizada.
                // Vou criar como Finalizada para ele ganhar os pontos.

                System.out.println("Gerando BYE para time: " + timeId);
                Partida bye = new Partida();
                bye.setTime1(timeCache.get(timeId));
                bye.setTime2(null); // Bye
                bye.setFase(fase);
                bye.setRodada(proximaRodada);
                bye.setStatus(StatusPartida.FINALIZADA);
                bye.setPlacarTime1(1); // 1x0 simbólico ou W.O
                bye.setPlacarTime2(0);
                bye.setVencedor(timeCache.get(timeId)); // Define vencedor
                bye.setIdentificadorBracket("S-R" + proximaRodada + "-BYE");
                novasPartidas.add(bye);

                timesJaPareados.add(timeId);
            }
        }

        System.out.println("Total partidas geradas para rodada " + proximaRodada + ": " + novasPartidas.size());
        return novasPartidas;
    }

    private Long encontrarAdversario(Long timeId, List<Long> timesOrdenados,
            Map<Long, Integer> pontuacoes, Set<String> confrontosRealizados,
            Set<Long> timesJaPareados) {

        int pontuacaoTime = pontuacoes.getOrDefault(timeId, 0);

        // Prioriza times com pontuação similar
        return timesOrdenados.stream()
                .filter(tId -> !tId.equals(timeId))
                .filter(tId -> !timesJaPareados.contains(tId))
                .filter(tId -> !confrontosRealizados.contains(gerarChaveConfronto(timeId, tId)))
                .min(Comparator.comparingInt(tId -> Math.abs(pontuacoes.getOrDefault(tId, 0) - pontuacaoTime)))
                .orElse(null);
    }

    private Map<Long, Integer> calcularPontuacoes(List<Partida> partidas) {
        Map<Long, Integer> pontuacoes = new HashMap<>();

        for (Partida partida : partidas) {
            if (!partida.isFinalizada())
                continue;

            Long t1 = partida.getTime1().getId();
            Long t2 = (partida.getTime2() != null) ? partida.getTime2().getId() : null;

            pontuacoes.putIfAbsent(t1, 0);
            if (t2 != null)
                pontuacoes.putIfAbsent(t2, 0);

            if (partida.getVencedor() != null) {
                Long v = partida.getVencedor().getId();
                pontuacoes.merge(v, 3, Integer::sum);
            } else {
                // Empate? Suíço normalmente não tem empate puro sem pontos, mas se 1-1:
                // Vou assumir 1 ponto cada
                pontuacoes.merge(t1, 1, Integer::sum);
                if (t2 != null)
                    pontuacoes.merge(t2, 1, Integer::sum);
            }
        }

        return pontuacoes;
    }

    private Set<String> getConfrontosRealizados(List<Partida> partidas) {
        Set<String> confrontos = new HashSet<>();
        for (Partida partida : partidas) {
            String chave = gerarChaveConfronto(partida.getTime1().getId(),
                    partida.getTime2() != null ? partida.getTime2().getId() : -1L);
            confrontos.add(chave);
        }
        return confrontos;
    }

    // Método auxiliar para chave com IDs
    private String gerarChaveConfronto(Long id1, Long id2) {
        if (id1 == null || id2 == null || id2 == -1L)
            return "BYE"; // -1L para indicar bye
        return id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    private String gerarChaveConfronto(Time t1, Time t2) {
        long id1 = t1.getId();
        long id2 = t2.getId();
        return id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    @Override
    public List<Time> calcularClassificados(Fase fase) {
        // Usa a lista de partidas da fase (pode vir lazy, mas dentro de transação
        // funciona)
        List<Partida> partidas = fase.getPartidas();
        Map<Long, Integer> pontuacoesId = calcularPontuacoes(partidas);

        // Mapeia de volta para objetos Time para retorno
        // (Isso assume que fase.getCampeonato().getTimesParticipantes() conteem os
        // times)
        Map<Long, Time> timeMap = new HashMap<>();
        fase.getCampeonato().getTimesParticipantes().forEach(t -> timeMap.put(t.getId(), t));

        int classificados = fase.getClassificadosNecessarios() != null
                ? fase.getClassificadosNecessarios()
                : pontuacoesId.size() / 2;

        return pontuacoesId.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(classificados)
                .map(e -> timeMap.get(e.getKey())) // Retorna o objeto Time
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validarNumeroTimes(int quantidade) {
        return quantidade >= 4;
    }

    @Override
    public String getMensagemValidacao(int quantidade) {
        if (validarNumeroTimes(quantidade)) {
            int rodadas = Math.min(quantidade - 1, RODADAS_PADRAO);
            return String.format(
                    "OK: Sistema suíço com %d times terá %d rodadas.",
                    quantidade, rodadas);
        }
        return "Sistema suíço requer no mínimo 4 times.";
    }

    @Override
    public int getMinTimes() {
        return 4;
    }

    @Override
    public int getMaxTimes() {
        return 0; // Sem limite
    }
}
