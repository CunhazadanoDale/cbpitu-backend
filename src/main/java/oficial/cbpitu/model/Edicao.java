package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_edicoes")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Edicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Integer ano; // Ex: 2024

    @Column(nullable = false)
    private Integer numeroEdicao = 1; // Para múltiplas edições no mesmo ano (1, 2, etc.)

    @Column(nullable = false)
    private String nome; // Ex: "CBPITU 2024" ou "CBPITU 2024 - Edição de Verão"

    private LocalDate dataInicio;
    private LocalDate dataFim;

    @Column(length = 500)
    private String descricao;

    // Campeonatos desta edição
    @OneToMany(mappedBy = "edicao", cascade = CascadeType.ALL)
    @OrderBy("dataInicio ASC")
    private List<Campeonato> campeonatos = new ArrayList<>();

    // Escalações dos times nesta edição
    @OneToMany(mappedBy = "edicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Escalacao> escalacoes = new ArrayList<>();

    // Métodos utilitários
    public void adicionarCampeonato(Campeonato campeonato) {
        campeonato.setEdicao(this);
        this.campeonatos.add(campeonato);
    }

    public void adicionarEscalacao(Escalacao escalacao) {
        escalacao.setEdicao(this);
        this.escalacoes.add(escalacao);
    }

    public String getNomeCompleto() {
        if (numeroEdicao > 1) {
            return nome + " - Edição " + numeroEdicao;
        }
        return nome;
    }
}
