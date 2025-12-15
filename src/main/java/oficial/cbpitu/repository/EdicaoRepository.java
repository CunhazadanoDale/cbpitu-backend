package oficial.cbpitu.repository;

import oficial.cbpitu.model.Edicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EdicaoRepository extends JpaRepository<Edicao, Long> {
    
    List<Edicao> findByAnoOrderByNumeroEdicaoAsc(Integer ano);
    
    Optional<Edicao> findByAnoAndNumeroEdicao(Integer ano, Integer numeroEdicao);
    
    List<Edicao> findAllByOrderByAnoDescNumeroEdicaoDesc();
    
    boolean existsByAnoAndNumeroEdicao(Integer ano, Integer numeroEdicao);
}
