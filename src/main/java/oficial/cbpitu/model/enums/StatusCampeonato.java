package oficial.cbpitu.model.enums;

/**
 * Status geral do campeonato.
 */
public enum StatusCampeonato {

    RASCUNHO("Rascunho", "Campeonato em fase de planejamento"),
    INSCRICOES_ABERTAS("Inscrições Abertas", "Aceitando times participantes"),
    INSCRICOES_ENCERRADAS("Inscrições Encerradas", "Não aceita mais times"),
    EM_ANDAMENTO("Em Andamento", "Competição ativa"),
    PAUSADO("Pausado", "Temporariamente interrompido"),
    FINALIZADO("Finalizado", "Campeonato concluído"),
    CANCELADO("Cancelado", "Campeonato cancelado");

    private final String nome;
    private final String descricao;

    StatusCampeonato(String nome, String descricao) {
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
