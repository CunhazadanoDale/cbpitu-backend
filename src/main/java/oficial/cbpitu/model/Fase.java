package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_fases")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Fase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nome; // Ex: "Fase de Grupos", "Quartas de Final", "Semifinal", "Final"

    private Integer ordem; // Sequência da fase (1, 2, 3...)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoCompeticao formato;

    @ManyToOne
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

    // Grupos desta fase (se formato for GRUPOS ou similar)
    @OneToMany(mappedBy = "fase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grupo> grupos = new ArrayList<>();

    // Partidas diretas (para mata-mata)
    @OneToMany(mappedBy = "fase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rodada ASC, id ASC")
    private List<Partida> partidas = new ArrayList<>();

    // Configurações da fase
    private Integer classificadosNecessarios; // Quantos times avançam
    private Integer rodadasTotais; // Para sistema suíço
    private Boolean finalizada = false;

    // Métodos utilitários
    public void adicionarGrupo(Grupo grupo) {
        grupo.setFase(this);
        this.grupos.add(grupo);
    }

    public void adicionarPartida(Partida partida) {
        partida.setFase(this);
        this.partidas.add(partida);
    }

    public boolean isGrupos() {
        return this.formato == FormatoCompeticao.GRUPOS
                || this.formato == FormatoCompeticao.GRUPOS_IDA_VOLTA;
    }

    public boolean isMataMata() {
        return this.formato == FormatoCompeticao.MATA_MATA
                || this.formato == FormatoCompeticao.MATA_MATA_MD3
                || this.formato == FormatoCompeticao.MATA_MATA_MD5;
    }
}
