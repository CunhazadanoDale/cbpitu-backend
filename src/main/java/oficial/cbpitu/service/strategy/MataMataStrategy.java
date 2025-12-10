package oficial.cbpitu.service.strategy;

import oficial.cbpitu.model.Fase;
import oficial.cbpitu.model.Partida;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.model.enums.StatusPartida;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Strategy para formato Mata-Mata (eliminação simples).
 */
@Component
public class MataMataStrategy implements GeradorDeConfrontos {

    @Override
    public List<Partida> gerarConfrontos(List<Time> times, Fase fase) {
        List<Partida> partidas = new ArrayList<>();
        List<Time> timesSorteados = new ArrayList<>(times);
        Collections.shuffle(timesSorteados);

        int numTimes = timesSorteados.size();
        int rodada = 1;

        // Gera confrontos da primeira rodada
        for (int i = 0; i < numTimes; i += 2) {
            Partida partida = new Partida();
            partida.setTime1(timesSorteados.get(i));
            partida.setTime2(timesSorteados.get(i + 1));
            partida.setFase(fase);
            partida.setRodada(rodada);
            partida.setStatus(StatusPartida.PENDENTE);

            int numPartidas = numTimes / 2;
            partida.setIdentificadorBracket(gerarNomeFase(numPartidas) + " " + ((i / 2) + 1));
            partidas.add(partida);
        }

        return partidas;
    }

    /**
     * Gera partidas da próxima rodada com os vencedores.
     */
    public List<Partida> gerarProximaRodada(Fase fase, List<Time> vencedores) {
        List<Partida> partidas = new ArrayList<>();

        // Descobre rodada atual baseado na última partida
        int rodadaAtual = 0;
        if (!fase.getPartidas().isEmpty()) {
            rodadaAtual = fase.getPartidas().stream()
                    .mapToInt(Partida::getRodada)
                    .max()
                    .orElse(0);
        }

        int proximaRodada = rodadaAtual + 1;
        int numMatches = vencedores.size() / 2;

        for (int i = 0; i < vencedores.size(); i += 2) {
            Partida partida = new Partida();
            partida.setTime1(vencedores.get(i));
            partida.setTime2(vencedores.get(i + 1));
            partida.setFase(fase);
            partida.setRodada(proximaRodada);
            partida.setStatus(StatusPartida.PENDENTE);

            partida.setIdentificadorBracket(gerarNomeFase(numMatches) + " " + ((i / 2) + 1));
            partidas.add(partida);
        }

        return partidas;
    }

    @Override
    public List<Time> calcularClassificados(Fase fase) {
        // No mata-mata, o classificado é o vencedor da final
        List<Time> classificados = new ArrayList<>();

        // No mata-mata, o classificado é o vencedor da final (última rodada)

        fase.getPartidas().stream()
                .filter(Partida::isFinalizada)
                .max(java.util.Comparator.comparingInt(Partida::getRodada))
                .ifPresent(final_ -> {
                    if (final_.getVencedor() != null) {
                        classificados.add(final_.getVencedor());
                    }
                });

        return classificados;
    }

    @Override
    public boolean validarNumeroTimes(int quantidade) {
        // Deve ser potência de 2: 2, 4, 8, 16, 32...
        return quantidade >= 2 && (quantidade & (quantidade - 1)) == 0;
    }

    @Override
    public String getMensagemValidacao(int quantidade) {
        if (validarNumeroTimes(quantidade)) {
            return "OK: " + quantidade + " times é válido para mata-mata.";
        }

        int proximaPotencia = proximaPotenciaDe2(quantidade);
        int potenciaAnterior = proximaPotencia / 2;

        return String.format(
                "Mata-mata requer potência de 2 (2, 4, 8, 16...). " +
                        "Você tem %d times. Sugestões: adicionar %d times para ter %d, " +
                        "ou remover %d times para ter %d.",
                quantidade,
                proximaPotencia - quantidade, proximaPotencia,
                quantidade - potenciaAnterior, potenciaAnterior);
    }

    @Override
    public int getMinTimes() {
        return 2;
    }

    @Override
    public int getMaxTimes() {
        return 64; // Limite prático
    }

    // Métodos auxiliares
    private int proximaPotenciaDe2(int n) {
        int potencia = 1;
        while (potencia < n) {
            potencia *= 2;
        }
        return potencia;
    }

    private String gerarNomeFase(int numPartidas) {
        return switch (numPartidas) {
            case 1 -> "FINAL";
            case 2 -> "SEMIFINAL";
            case 4 -> "QUARTAS";
            case 8 -> "OITAVAS";
            default -> "RODADA DE " + (numPartidas * 2);
        };
    }
}
