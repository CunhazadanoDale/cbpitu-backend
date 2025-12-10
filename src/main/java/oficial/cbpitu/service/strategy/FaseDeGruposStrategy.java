package oficial.cbpitu.service.strategy;

import oficial.cbpitu.model.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;
import oficial.cbpitu.model.enums.StatusPartida;
import oficial.cbpitu.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy para formato Fase de Grupos.
 */
@Component
@lombok.RequiredArgsConstructor
public class FaseDeGruposStrategy implements GeradorDeConfrontos {

    private static final int TIMES_POR_GRUPO_PADRAO = 4;

    private final PartidaRepository partidaRepository;

    @Override
    public List<Partida> gerarConfrontos(List<Time> times, Fase fase) {
        List<Partida> todasPartidas = new ArrayList<>();

        // Divide os times em grupos
        List<Grupo> grupos = dividirEmGrupos(times, fase);

        // Gera partidas para cada grupo
        for (Grupo grupo : grupos) {
            List<Time> timesDogGrupo = new ArrayList<>(grupo.getTimes());
            List<Partida> partidasDoGrupo = gerarRoundRobin(timesDogGrupo, fase, grupo);
            todasPartidas.addAll(partidasDoGrupo);
        }

        return todasPartidas;
    }

    /**
     * Gera partidas para grupos já definidos.
     */
    public List<Partida> gerarPartidasParaGrupos(List<Grupo> grupos, Fase fase) {
        List<Partida> todasPartidas = new ArrayList<>();
        for (Grupo grupo : grupos) {
            List<Time> timesDoGrupo = new ArrayList<>(grupo.getTimes());
            List<Partida> partidasDoGrupo = gerarRoundRobin(timesDoGrupo, fase, grupo);
            todasPartidas.addAll(partidasDoGrupo);
        }
        return todasPartidas;
    }

    /**
     * Divide os times em grupos de forma equilibrada.
     */
    public List<Grupo> dividirEmGrupos(List<Time> times, Fase fase) {
        List<Grupo> grupos = new ArrayList<>();
        List<Time> timesSorteados = new ArrayList<>(times);
        Collections.shuffle(timesSorteados);

        int numGrupos = calcularNumeroGrupos(times.size());
        int timesPorGrupo = (int) Math.ceil((double) times.size() / numGrupos);

        char letraGrupo = 'A';
        for (int i = 0; i < numGrupos; i++) {
            Grupo grupo = new Grupo();
            grupo.setNome("Grupo " + letraGrupo);
            grupo.setFase(fase);

            int inicio = i * timesPorGrupo;
            int fim = Math.min(inicio + timesPorGrupo, timesSorteados.size());

            for (int j = inicio; j < fim; j++) {
                grupo.adicionarTime(timesSorteados.get(j));
            }

            grupos.add(grupo);
            letraGrupo++;
        }

        return grupos;
    }

    /**
     * Gera round-robin (todos contra todos) para um grupo.
     */
    private List<Partida> gerarRoundRobin(List<Time> times, Fase fase, Grupo grupo) {
        List<Partida> partidas = new ArrayList<>();
        int numTimes = times.size();
        boolean idaEVolta = fase.getFormato() == FormatoCompeticao.GRUPOS_IDA_VOLTA;

        int rodada = 1;
        for (int i = 0; i < numTimes - 1; i++) {
            for (int j = i + 1; j < numTimes; j++) {
                // Jogo de ida
                Partida partidaIda = new Partida();
                partidaIda.setTime1(times.get(i));
                partidaIda.setTime2(times.get(j));
                partidaIda.setFase(fase);
                partidaIda.setGrupo(grupo);
                partidaIda.setRodada(rodada);
                partidaIda.setStatus(StatusPartida.PENDENTE);
                partidas.add(partidaIda);

                // Jogo de volta (se configurado)
                if (idaEVolta) {
                    Partida partidaVolta = new Partida();
                    partidaVolta.setTime1(times.get(j)); // Inverte mandante
                    partidaVolta.setTime2(times.get(i));
                    partidaVolta.setFase(fase);
                    partidaVolta.setGrupo(grupo);
                    partidaVolta.setRodada(rodada + numTimes - 1);
                    partidaVolta.setStatus(StatusPartida.PENDENTE);
                    partidas.add(partidaVolta);
                }
            }
            rodada++;
        }

        Collections.shuffle(partidas);
        return partidas;
    }

    @Override
    public List<Time> calcularClassificados(Fase fase) {
        List<Time> classificados = new ArrayList<>();

        // Calcula quantos classificados por grupo
        // Se a divisão der 0 (ex: 1 vaga / 2 grupos), assume o padrão de 2 por grupo
        int classificadosPorGrupo = fase.getClassificadosNecessarios() != null
                ? fase.getClassificadosNecessarios() / fase.getGrupos().size()
                : 2;

        if (classificadosPorGrupo < 1) {
            classificadosPorGrupo = 2;
        }

        if (fase.getGrupos() == null)
            return classificados;

        for (Grupo grupo : fase.getGrupos()) {
            List<Partida> partidasDoGrupo = partidaRepository.findByGrupoId(grupo.getId());
            List<ClassificacaoGrupo> tabela = calcularTabelaGrupo(grupo, partidasDoGrupo);

            // Pega os N melhores de cada grupo
            for (int i = 0; i < Math.min(classificadosPorGrupo, tabela.size()); i++) {
                classificados.add(tabela.get(i).getTime());
            }
        }

        return classificados;
    }

    /**
     * Calcula a tabela de classificação de um grupo.
     */
    public List<ClassificacaoGrupo> calcularTabelaGrupo(Grupo grupo, List<Partida> partidasDoGrupo) {
        Map<Time, ClassificacaoGrupo> classificacoes = new HashMap<>();

        // Inicializa classificação para cada time
        for (Time time : grupo.getTimes()) {
            classificacoes.put(time, new ClassificacaoGrupo(time));
        }

        // Processa cada partida finalizada
        for (Partida partida : partidasDoGrupo) {
            if (!partida.isFinalizada())
                continue;

            ClassificacaoGrupo c1 = classificacoes.get(partida.getTime1());
            ClassificacaoGrupo c2 = classificacoes.get(partida.getTime2());

            if (c1 == null || c2 == null)
                continue;

            c1.adicionarJogo();
            c2.adicionarJogo();

            c1.adicionarGolsPro(partida.getPlacarTime1());
            c1.adicionarGolsContra(partida.getPlacarTime2());
            c2.adicionarGolsPro(partida.getPlacarTime2());
            c2.adicionarGolsContra(partida.getPlacarTime1());

            if (partida.getPlacarTime1() > partida.getPlacarTime2()) {
                c1.adicionarVitoria();
                c2.adicionarDerrota();
            } else if (partida.getPlacarTime2() > partida.getPlacarTime1()) {
                c2.adicionarVitoria();
                c1.adicionarDerrota();
            } else {
                c1.adicionarEmpate();
                c2.adicionarEmpate();
            }
        }

        // Ordena por pontos, saldo, gols pró
        return classificacoes.values().stream()
                .sorted(Comparator
                        .comparingInt(ClassificacaoGrupo::getPontos).reversed()
                        .thenComparingInt(ClassificacaoGrupo::getSaldoGols).reversed()
                        .thenComparingInt(ClassificacaoGrupo::getGolsPro).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean validarNumeroTimes(int quantidade) {
        return quantidade >= 4;
    }

    @Override
    public String getMensagemValidacao(int quantidade) {
        if (validarNumeroTimes(quantidade)) {
            int numGrupos = calcularNumeroGrupos(quantidade);
            int timesPorGrupo = quantidade / numGrupos;
            return String.format(
                    "OK: %d times serão divididos em %d grupo(s) com ~%d times cada.",
                    quantidade, numGrupos, timesPorGrupo);
        }
        return "Fase de grupos requer no mínimo 4 times.";
    }

    @Override
    public int getMinTimes() {
        return 4;
    }

    @Override
    public int getMaxTimes() {
        return 0; // Sem limite
    }

    private int calcularNumeroGrupos(int numTimes) {
        // Ajustado para criar grupos maiores (estilo "A e B")
        // Até 7 times: 1 grupo
        if (numTimes <= 7)
            return 1;
        // Até 16 times: 2 grupos (A e B)
        if (numTimes <= 16)
            return 2;
        // Acima disso, tenta manter grupos de ~8 times
        return numTimes / 8;
    }

    // Classe auxiliar para classificação
    public static class ClassificacaoGrupo {
        private final Time time;
        private int jogos = 0;
        private int vitorias = 0;
        private int empates = 0;
        private int derrotas = 0;
        private int golsPro = 0;
        private int golsContra = 0;

        public ClassificacaoGrupo(Time time) {
            this.time = time;
        }

        public Time getTime() {
            return time;
        }

        public int getJogos() {
            return jogos;
        }

        public int getVitorias() {
            return vitorias;
        }

        public int getEmpates() {
            return empates;
        }

        public int getDerrotas() {
            return derrotas;
        }

        public int getGolsPro() {
            return golsPro;
        }

        public int getGolsContra() {
            return golsContra;
        }

        public int getPontos() {
            return (vitorias * 3) + empates;
        }

        public int getSaldoGols() {
            return golsPro - golsContra;
        }

        public void adicionarJogo() {
            jogos++;
        }

        public void adicionarVitoria() {
            vitorias++;
        }

        public void adicionarEmpate() {
            empates++;
        }

        public void adicionarDerrota() {
            derrotas++;
        }

        public void adicionarGolsPro(int gols) {
            golsPro += gols;
        }

        public void adicionarGolsContra(int gols) {
            golsContra += gols;
        }
    }
}
