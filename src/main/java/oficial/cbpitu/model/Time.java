package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_times")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // informacoes do time no geral
    private String nomeTime;
    private String trofeus;

    @ElementCollection
    private Set<Long> campeonatosParticipados = new HashSet<>();

    // usuarios
    @OneToOne
    @JoinColumn(name = "capitao_id")
    private Jogador capitao;

    @ManyToMany
    @JoinTable(name = "tb_time_jogador", joinColumns = @JoinColumn(name = "time_id"), inverseJoinColumns = @JoinColumn(name = "jogador_id"))
    private Set<Jogador> jogadores = new HashSet<>();
}
