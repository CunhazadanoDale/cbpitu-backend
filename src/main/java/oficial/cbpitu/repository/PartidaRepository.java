package oficial.cbpitu.repository;

import oficial.cbpitu.model.Partida;
import oficial.cbpitu.model.enums.StatusPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findByFaseId(Long faseId);

    List<Partida> findByGrupoId(Long grupoId);

    List<Partida> findByStatus(StatusPartida status);

    List<Partida> findByFaseIdAndRodada(Long faseId, Integer rodada);

    List<Partida> findByGrupoIdAndRodada(Long grupoId, Integer rodada);

    @Query("SELECT p FROM Partida p WHERE p.fase.campeonato.id = :campeonatoId")
    List<Partida> findByCampeonatoId(Long campeonatoId);

    @Query("SELECT p FROM Partida p WHERE p.fase.campeonato.id = :campeonatoId AND p.status = :status")
    List<Partida> findByCampeonatoIdAndStatus(Long campeonatoId, StatusPartida status);

    @Query("SELECT p FROM Partida p WHERE (p.time1.id = :timeId OR p.time2.id = :timeId)")
    List<Partida> findByTimeId(Long timeId);

    @Query("SELECT p FROM Partida p WHERE p.fase.campeonato.id = :campeonatoId AND (p.time1.id = :timeId OR p.time2.id = :timeId)")
    List<Partida> findByCampeonatoIdAndTimeId(Long campeonatoId, Long timeId);

    @Query("SELECT COUNT(p) FROM Partida p WHERE p.fase.id = :faseId AND p.status = 'FINALIZADA'")
    Long countFinalizadasByFaseId(Long faseId);

    @Query("SELECT COUNT(p) FROM Partida p WHERE p.fase.id = :faseId")
    Long countByFaseId(Long faseId);
}
