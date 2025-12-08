package oficial.cbpitu.repository;

import oficial.cbpitu.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    List<Grupo> findByFaseId(Long faseId);

    List<Grupo> findByFaseIdOrderByNomeAsc(Long faseId);
}
