package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_jogadores")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nickname;
    private String nomeReal;
    private String laneLol; // lane (TOP, JUNGLE, MID, ADC, SUPPORT)

    @ManyToMany(mappedBy = "jogadores")
    private Set<Time> times = new HashSet<>();
}
