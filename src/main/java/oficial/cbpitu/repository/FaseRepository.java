package oficial.cbpitu.repository;

import oficial.cbpitu.model.Fase;
import oficial.cbpitu.model.enums.FormatoCompeticao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaseRepository extends JpaRepository<Fase, Long> {

    List<Fase> findByCampeonatoIdOrderByOrdemAsc(Long campeonatoId);

    Optional<Fase> findByCampeonatoIdAndOrdem(Long campeonatoId, Integer ordem);

    List<Fase> findByCampeonatoIdAndFormato(Long campeonatoId, FormatoCompeticao formato);

    Optional<Fase> findFirstByCampeonatoIdAndFinalizadaFalseOrderByOrdemAsc(Long campeonatoId);
}
