package oficial.cbpitu.model.enums;

/**
 * Define os formatos de competição disponíveis no sistema.
 */
public enum FormatoCompeticao {

    MATA_MATA("Mata-Mata", "Eliminação simples - perdeu, está fora"),
    MATA_MATA_MD3("Mata-Mata MD3", "Melhor de 3 partidas"),
    MATA_MATA_MD5("Mata-Mata MD5", "Melhor de 5 partidas"),
    GRUPOS("Fase de Grupos", "Todos contra todos dentro do grupo"),
    GRUPOS_IDA_VOLTA("Grupos Ida e Volta", "Dois jogos por confronto no grupo"),
    SISTEMA_SUICO("Sistema Suíço", "Pareamento por pontuação similar"),
    LOSER_BRACKET("Loser Bracket", "Double elimination com bracket de perdedores");

    private final String nome;
    private final String descricao;

    FormatoCompeticao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
