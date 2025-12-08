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
     */
    public List<Partida> gerarProximaRodada(Fase fase, int rodadaAtual) {
        List<Partida> novasPartidas = new ArrayList<>();
        Map<Time, Integer> pontuacoes = calcularPontuacoes(fase);
        Set<String> confrontosJaRealizados = getConfrontosRealizados(fase);

        // Ordena times por pontuação (maior para menor)
        List<Time> timesOrdenados = pontuacoes.entrySet().stream()
                .sorted(Map.Entry.<Time, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Set<Time> timesJaPareados = new HashSet<>();
        int proximaRodada = rodadaAtual + 1;

        for (Time time : timesOrdenados) {
            if (timesJaPareados.contains(time))
                continue;

            // Procura adversário com pontuação similar que ainda não enfrentou
            Time adversario = encontrarAdversario(time, timesOrdenados, pontuacoes,
                    confrontosJaRealizados, timesJaPareados);

            if (adversario != null) {
                Partida partida = new Partida();
                partida.setTime1(time);
                partida.setTime2(adversario);
                partida.setFase(fase);
                partida.setRodada(proximaRodada);
                partida.setStatus(StatusPartida.PENDENTE);
                novasPartidas.add(partida);

                timesJaPareados.add(time);
                timesJaPareados.add(adversario);
                confrontosJaRealizados.add(gerarChaveConfronto(time, adversario));
            }
        }

        return novasPartidas;
    }

    private Time encontrarAdversario(Time time, List<Time> timesOrdenados,
            Map<Time, Integer> pontuacoes, Set<String> confrontosRealizados,
            Set<Time> timesJaPareados) {

        int pontuacaoTime = pontuacoes.getOrDefault(time, 0);

        // Prioriza times com pontuação similar
        return timesOrdenados.stream()
                .filter(t -> !t.equals(time))
                .filter(t -> !timesJaPareados.contains(t))
                .filter(t -> !confrontosRealizados.contains(gerarChaveConfronto(time, t)))
                .min(Comparator.comparingInt(t -> Math.abs(pontuacoes.getOrDefault(t, 0) - pontuacaoTime)))
                .orElse(null);
    }

    private Map<Time, Integer> calcularPontuacoes(Fase fase) {
        Map<Time, Integer> pontuacoes = new HashMap<>();

        for (Partida partida : fase.getPartidas()) {
            if (!partida.isFinalizada())
                continue;

            pontuacoes.putIfAbsent(partida.getTime1(), 0);
            pontuacoes.putIfAbsent(partida.getTime2(), 0);

            if (partida.getVencedor() != null) {
                pontuacoes.merge(partida.getVencedor(), 3, Integer::sum);
            } else {
                pontuacoes.merge(partida.getTime1(), 1, Integer::sum);
                pontuacoes.merge(partida.getTime2(), 1, Integer::sum);
            }
        }

        return pontuacoes;
    }

    private Set<String> getConfrontosRealizados(Fase fase) {
        Set<String> confrontos = new HashSet<>();
        for (Partida partida : fase.getPartidas()) {
            confrontos.add(gerarChaveConfronto(partida.getTime1(), partida.getTime2()));
        }
        return confrontos;
    }

    private String gerarChaveConfronto(Time t1, Time t2) {
        long id1 = t1.getId();
        long id2 = t2.getId();
        return id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    @Override
    public List<Time> calcularClassificados(Fase fase) {
        Map<Time, Integer> pontuacoes = calcularPontuacoes(fase);
        int classificados = fase.getClassificadosNecessarios() != null
                ? fase.getClassificadosNecessarios()
                : pontuacoes.size() / 2;

        return pontuacoes.entrySet().stream()
                .sorted(Map.Entry.<Time, Integer>comparingByValue().reversed())
                .limit(classificados)
                .map(Map.Entry::getKey)
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
