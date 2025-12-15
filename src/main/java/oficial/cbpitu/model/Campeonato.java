package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;
import oficial.cbpitu.model.enums.StatusCampeonato;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_campeonatos")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Campeonato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 500)
    private String descricao;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCampeonato status = StatusCampeonato.RASCUNHO;

    // Times inscritos no campeonato
    @ManyToMany
    @JoinTable(name = "tb_campeonato_time", joinColumns = @JoinColumn(name = "campeonato_id"), inverseJoinColumns = @JoinColumn(name = "time_id"))
    private Set<Time> timesParticipantes = new HashSet<>();

    // Fases do campeonato (grupos, mata-mata, etc.)
    @OneToMany(mappedBy = "campeonato", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<Fase> fases = new ArrayList<>();

    // Limite de times (0 = sem limite)
    private Integer limiteMaximoTimes = 0;

    // Time campeão (definido ao final)
    @ManyToOne
    @JoinColumn(name = "campeao_id")
    private Time campeao;

    // Edição do campeonato (opcional - para associar a uma temporada)
    @ManyToOne
    @JoinColumn(name = "edicao_id")
    private Edicao edicao;

    // Métodos utilitários
    public void adicionarTime(Time time) {
        this.timesParticipantes.add(time);
    }

    public void removerTime(Time time) {
        this.timesParticipantes.remove(time);
    }

    public void adicionarFase(Fase fase) {
        fase.setCampeonato(this);
        this.fases.add(fase);
    }

    public int getNumeroTimesInscritos() {
        return this.timesParticipantes.size();
    }

    public boolean podeInscreverTime() {
        if (this.status != StatusCampeonato.INSCRICOES_ABERTAS) {
            return false;
        }
        if (this.limiteMaximoTimes > 0 && this.timesParticipantes.size() >= this.limiteMaximoTimes) {
            return false;
        }
        return true;
    }
}
