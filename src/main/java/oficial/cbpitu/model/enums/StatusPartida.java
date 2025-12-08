package oficial.cbpitu.model.enums;

/**
 * Status de uma partida individual.
 */
public enum StatusPartida {

    PENDENTE("Pendente", "Aguardando agendamento ou início"),
    AGENDADA("Agendada", "Data e hora definidas"),
    EM_ANDAMENTO("Em Andamento", "Partida sendo disputada"),
    FINALIZADA("Finalizada", "Resultado registrado"),
    CANCELADA("Cancelada", "Partida cancelada"),
    ADIADA("Adiada", "Partida adiada para nova data"),
    WO("W.O.", "Vitória por ausência do adversário");

    private final String nome;
    private final String descricao;

    StatusPartida(String nome, String descricao) {
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
