package oficial.cbpitu.service.strategy;

import oficial.cbpitu.model.Fase;
import oficial.cbpitu.model.Partida;
import oficial.cbpitu.model.Time;

import java.util.List;

/**
 * Interface para estratégias de geração de confrontos.
 * Cada formato de competição implementa esta interface.
 */
public interface GeradorDeConfrontos {

    /**
     * Gera as partidas/confrontos para uma fase.
     * 
     * @param times Lista de times participantes
     * @param fase  Fase onde serão criadas as partidas
     * @return Lista de partidas geradas
     */
    List<Partida> gerarConfrontos(List<Time> times, Fase fase);

    /**
     * Calcula quais times se classificam após a fase terminar.
     * 
     * @param fase Fase finalizada
     * @return Lista de times classificados
     */
    List<Time> calcularClassificados(Fase fase);

    /**
     * Valida se o número de times é compatível com o formato.
     * 
     * @param quantidade Número de times
     * @return true se válido
     */
    boolean validarNumeroTimes(int quantidade);

    /**
     * Retorna mensagem explicando requisitos de quantidade de times.
     * 
     * @param quantidade Número de times atual
     * @return Mensagem de validação/sugestão
     */
    String getMensagemValidacao(int quantidade);

    /**
     * Retorna o número mínimo de times necessário.
     */
    int getMinTimes();

    /**
     * Retorna o número máximo de times suportado (0 = sem limite).
     */
    int getMaxTimes();
}
