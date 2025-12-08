package oficial.cbpitu.model;

import jakarta.persistence.*;
import lombok.*;
import oficial.cbpitu.model.enums.StatusPartida;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_partidas")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "time1_id")
    private Time time1; // mandante ou seed superior

    @ManyToOne
    @JoinColumn(name = "time2_id")
    private Time time2; // visitante ou seed inferior

    private Integer placarTime1 = 0;
    private Integer placarTime2 = 0;

    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPartida status = StatusPartida.PENDENTE;

    // Fase (para mata-mata ou se nao tiver grupo)
    @ManyToOne
    @JoinColumn(name = "fase_id")
    private Fase fase;

    // Grupo (se for fase de grupos)
    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    // Numero da rodada (1, 2, 3...)
    private Integer rodada;

    // Identificador do bracket (para mata-mata) - ex: "QF1", "SF1", "F"
    private String identificadorBracket;

    // Para séries MD3/MD5
    private Integer vitoriasTime1 = 0;
    private Integer vitoriasTime2 = 0;
    private Integer jogosDaSerie; // 3 ou 5

    // Vencedor (calculado após resultado)
    @ManyToOne
    @JoinColumn(name = "vencedor_id")
    private Time vencedor;

    // Métodos utilitários
    public void registrarResultado(int placarTime1, int placarTime2) {
        this.placarTime1 = placarTime1;
        this.placarTime2 = placarTime2;
        this.status = StatusPartida.FINALIZADA;

        if (placarTime1 > placarTime2) {
            this.vencedor = this.time1;
        } else if (placarTime2 > placarTime1) {
            this.vencedor = this.time2;
        }
        // Em caso de empate, vencedor fica null
    }

    public void registrarResultadoSerie(int vitoriasTime1, int vitoriasTime2) {
        this.vitoriasTime1 = vitoriasTime1;
        this.vitoriasTime2 = vitoriasTime2;

        int vitoriasNecessarias = (this.jogosDaSerie != null) ? (this.jogosDaSerie / 2) + 1 : 2;

        if (vitoriasTime1 >= vitoriasNecessarias) {
            this.vencedor = this.time1;
            this.status = StatusPartida.FINALIZADA;
        } else if (vitoriasTime2 >= vitoriasNecessarias) {
            this.vencedor = this.time2;
            this.status = StatusPartida.FINALIZADA;
        }
    }

    public Time getPerdedor() {
        if (this.vencedor == null)
            return null;
        return this.vencedor.equals(this.time1) ? this.time2 : this.time1;
    }

    public boolean isEmpate() {
        return this.status == StatusPartida.FINALIZADA
                && this.placarTime1.equals(this.placarTime2)
                && this.vencedor == null;
    }

    public boolean isFinalizada() {
        return this.status == StatusPartida.FINALIZADA;
    }
}
