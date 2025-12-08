package oficial.cbpitu.repository;

import oficial.cbpitu.model.Campeonato;
import oficial.cbpitu.model.enums.StatusCampeonato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {

    List<Campeonato> findByStatus(StatusCampeonato status);

    List<Campeonato> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT c FROM Campeonato c WHERE c.status IN ('INSCRICOES_ABERTAS', 'EM_ANDAMENTO')")
    List<Campeonato> findCampeonatosAtivos();

    @Query("SELECT c FROM Campeonato c JOIN c.timesParticipantes t WHERE t.id = :timeId")
    List<Campeonato> findByTimeParticipante(Long timeId);
}
