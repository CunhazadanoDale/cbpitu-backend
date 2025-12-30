package oficial.cbpitu.repository;

import oficial.cbpitu.model.Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeRepository extends JpaRepository<Time, Long> {

    // Optional<Time> findByCapitao(String nomeCapita);
    
    java.util.List<Time> findTop4ByOrderByTrofeusDesc();
}
