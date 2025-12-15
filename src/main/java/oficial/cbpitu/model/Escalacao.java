package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_escalacoes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"time_id", "edicao_id"})
})
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Escalacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Time que esta escalação representa
    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private Time time;

    // Edição em que esta escalação é válida
    @ManyToOne
    @JoinColumn(name = "edicao_id", nullable = false)
    private Edicao edicao;

    // Jogadores desta escalação (roster para esta temporada)
    @ManyToMany
    @JoinTable(
        name = "tb_escalacao_jogador",
        joinColumns = @JoinColumn(name = "escalacao_id"),
        inverseJoinColumns = @JoinColumn(name = "jogador_id")
    )
    private Set<Jogador> jogadores = new HashSet<>();

    // Capitão desta escalação específica
    @ManyToOne
    @JoinColumn(name = "capitao_id")
    private Jogador capitao;

    // Métodos utilitários
    public void adicionarJogador(Jogador jogador) {
        this.jogadores.add(jogador);
    }

    public void removerJogador(Jogador jogador) {
        this.jogadores.remove(jogador);
    }

    public int getNumeroJogadores() {
        return this.jogadores.size();
    }
}
