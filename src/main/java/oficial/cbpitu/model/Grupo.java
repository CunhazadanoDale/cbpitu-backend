package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_grupos")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nome; // Ex: "Grupo A", "Grupo B"

    @ManyToOne
    @JoinColumn(name = "fase_id", nullable = false)
    private Fase fase;

    // Times do grupo
    @ManyToMany
    @JoinTable(name = "tb_grupo_time", joinColumns = @JoinColumn(name = "grupo_id"), inverseJoinColumns = @JoinColumn(name = "time_id"))
    private Set<Time> times = new HashSet<>();

    // Partidas do grupo
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rodada ASC, id ASC")
    private List<Partida> partidas = new ArrayList<>();

    // Métodos utilitários
    public void adicionarTime(Time time) {
        this.times.add(time);
    }

    public void removerTime(Time time) {
        this.times.remove(time);
    }

    public void adicionarPartida(Partida partida) {
        partida.setGrupo(this);
        this.partidas.add(partida);
    }

    public int getNumeroTimes() {
        return this.times.size();
    }
}
