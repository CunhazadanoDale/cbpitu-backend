package oficial.cbpitu.repository;

import oficial.cbpitu.model.Escalacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscalacaoRepository extends JpaRepository<Escalacao, Long> {
    
    Optional<Escalacao> findByTimeIdAndEdicaoId(Long timeId, Long edicaoId);
    
    List<Escalacao> findByTimeIdOrderByEdicaoAnoDescEdicaoNumeroEdicaoDesc(Long timeId);
    
    List<Escalacao> findByEdicaoId(Long edicaoId);
    
    boolean existsByTimeIdAndEdicaoId(Long timeId, Long edicaoId);
}
