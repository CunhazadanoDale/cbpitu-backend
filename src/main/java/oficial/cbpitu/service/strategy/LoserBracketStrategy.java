package oficial.cbpitu.service.strategy;

import oficial.cbpitu.model.Fase;
import oficial.cbpitu.model.Partida;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.model.enums.StatusPartida;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Strategy para Loser Bracket (Double Elimination).
 * Teams têm duas chances - perdedores vão para bracket inferior.
 */
@Component
public class LoserBracketStrategy implements GeradorDeConfrontos {

    public static final String WINNERS_BRACKET = "WB";
    public static final String LOSERS_BRACKET = "LB";
    public static final String GRAND_FINALS = "GF";

    @Override
    public List<Partida> gerarConfrontos(List<Time> times, Fase fase) {
        List<Partida> partidas = new ArrayList<>();
        List<Time> timesSorteados = new ArrayList<>(times);
        Collections.shuffle(timesSorteados);

        // Gera primeira rodada do Winners Bracket
        int rodadaWB = calcularRodadaInicial(times.size());

        for (int i = 0; i < timesSorteados.size(); i += 2) {
            Partida partida = new Partida();
            partida.setTime1(timesSorteados.get(i));
            partida.setTime2(timesSorteados.get(i + 1));
            partida.setFase(fase);
            partida.setRodada(rodadaWB);
            partida.setStatus(StatusPartida.PENDENTE);
            partida.setIdentificadorBracket(WINNERS_BRACKET + "-R" + rodadaWB + "-" + ((i / 2) + 1));
            partidas.add(partida);
        }

        return partidas;
    }

    /**
     * Gera próxima rodada do Winners Bracket.
     */
    public List<Partida> gerarProximaRodadaWinners(Fase fase, List<Time> vencedores, int rodadaAtual) {
        List<Partida> partidas = new ArrayList<>();
        int proximaRodada = rodadaAtual - 1;

        for (int i = 0; i < vencedores.size(); i += 2) {
            Partida partida = new Partida();
            partida.setTime1(vencedores.get(i));
            partida.setTime2(vencedores.get(i + 1));
            partida.setFase(fase);
            partida.setRodada(proximaRodada);
            partida.setStatus(StatusPartida.PENDENTE);

            String identificador = proximaRodada == 1
                    ? WINNERS_BRACKET + "-FINAL"
                    : WINNERS_BRACKET + "-R" + proximaRodada + "-" + ((i / 2) + 1);
            partida.setIdentificadorBracket(identificador);
            partidas.add(partida);
        }

        return partidas;
    }

    /**
     * Gera partidas do Losers Bracket com os perdedores.
     */
    public List<Partida> gerarRodadaLosers(Fase fase, List<Time> perdedores, int rodadaLB) {
        List<Partida> partidas = new ArrayList<>();

        // Primeira rodada do LB: perdedores se enfrentam
        for (int i = 0; i < perdedores.size(); i += 2) {
            if (i + 1 >= perdedores.size())
                break; // Ímpar = bye

            Partida partida = new Partida();
            partida.setTime1(perdedores.get(i));
            partida.setTime2(perdedores.get(i + 1));
            partida.setFase(fase);
            partida.setRodada(100 + rodadaLB); // Rodadas LB: 101, 102, 103...
            partida.setStatus(StatusPartida.PENDENTE);
            partida.setIdentificadorBracket(LOSERS_BRACKET + "-R" + rodadaLB + "-" + ((i / 2) + 1));
            partidas.add(partida);
        }

        return partidas;
    }

    /**
     * Gera Grand Finals (vencedor WB vs vencedor LB).
     */
    public Partida gerarGrandFinals(Fase fase, Time vencedorWB, Time vencedorLB) {
        Partida grandFinals = new Partida();
        grandFinals.setTime1(vencedorWB); // Vantagem: vem do Winners
        grandFinals.setTime2(vencedorLB);
        grandFinals.setFase(fase);
        grandFinals.setRodada(0); // Rodada final
        grandFinals.setStatus(StatusPartida.PENDENTE);
        grandFinals.setIdentificadorBracket(GRAND_FINALS);
        return grandFinals;
    }

    /**
     * Gera Grand Finals Reset (se vencedor LB ganhar a primeira GF).
     */
    public Partida gerarGrandFinalsReset(Fase fase, Time time1, Time time2) {
        Partida reset = new Partida();
        reset.setTime1(time1);
        reset.setTime2(time2);
        reset.setFase(fase);
        reset.setRodada(-1); // Super final
        reset.setStatus(StatusPartida.PENDENTE);
        reset.setIdentificadorBracket(GRAND_FINALS + "-RESET");
        return reset;
    }

    @Override
    public List<Time> calcularClassificados(Fase fase) {
        // No double elimination, o campeão é o vencedor da Grand Finals
        List<Time> classificados = new ArrayList<>();

        fase.getPartidas().stream()
                .filter(p -> p.getIdentificadorBracket() != null)
                .filter(p -> p.getIdentificadorBracket().startsWith(GRAND_FINALS))
                .filter(Partida::isFinalizada)
                .max(Comparator.comparingInt(p -> p.getIdentificadorBracket().contains("RESET") ? 1 : 0))
                .ifPresent(finalMatch -> {
                    if (finalMatch.getVencedor() != null) {
                        classificados.add(finalMatch.getVencedor());
                    }
                });

        return classificados;
    }

    @Override
    public boolean validarNumeroTimes(int quantidade) {
        // Precisa ser potência de 2
        return quantidade >= 4 && (quantidade & (quantidade - 1)) == 0;
    }

    @Override
    public String getMensagemValidacao(int quantidade) {
        if (validarNumeroTimes(quantidade)) {
            return String.format(
                    "OK: Double elimination com %d times. " +
                            "Winners Bracket + Losers Bracket + Grand Finals.",
                    quantidade);
        }

        int proxima = proximaPotenciaDe2(quantidade);
        int anterior = proxima / 2;

        return String.format(
                "Loser bracket requer potência de 2 (4, 8, 16...). " +
                        "Você tem %d times. Ajuste para %d ou %d times.",
                quantidade, anterior, proxima);
    }

    @Override
    public int getMinTimes() {
        return 4;
    }

    @Override
    public int getMaxTimes() {
        return 32; // Limite prático
    }

    // Auxiliares
    private int calcularRodadaInicial(int numTimes) {
        int rodada = 0;
        int times = numTimes;
        while (times >= 2) {
            rodada++;
            times /= 2;
        }
        return rodada;
    }

    private int proximaPotenciaDe2(int n) {
        int p = 1;
        while (p < n)
            p *= 2;
        return p;
    }
}
