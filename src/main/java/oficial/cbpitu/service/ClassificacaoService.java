package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.model.Grupo;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.repository.GrupoRepository;
import oficial.cbpitu.service.strategy.FaseDeGruposStrategy;
import oficial.cbpitu.service.strategy.FaseDeGruposStrategy.ClassificacaoGrupo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassificacaoService {

    private final GrupoRepository grupoRepository;
    private final FaseDeGruposStrategy faseDeGruposStrategy;
    private final oficial.cbpitu.repository.PartidaRepository partidaRepository;

    /**
     * Retorna a tabela de classificação de um grupo.
     */
    public List<ClassificacaoGrupo> getTabelaGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado: " + grupoId));

        return faseDeGruposStrategy.calcularTabelaGrupo(grupo, partidaRepository.findByGrupoId(grupoId));
    }

    /**
     * Retorna a posição de um time específico no grupo.
     */
    public Optional<Integer> getPosicaoTime(Long grupoId, Long timeId) {
        List<ClassificacaoGrupo> tabela = getTabelaGrupo(grupoId);

        for (int i = 0; i < tabela.size(); i++) {
            if (tabela.get(i).getTime().getId().equals(timeId)) {
                return Optional.of(i + 1);
            }
        }

        return Optional.empty();
    }

    /**
     * Retorna os times classificados de um grupo.
     */
    public List<Time> getClassificadosGrupo(Long grupoId, int quantos) {
        List<ClassificacaoGrupo> tabela = getTabelaGrupo(grupoId);

        return tabela.stream()
                .limit(quantos)
                .map(ClassificacaoGrupo::getTime)
                .toList();
    }

    /**
     * Retorna as tabelas de todos os grupos de uma fase.
     */
    public List<List<ClassificacaoGrupo>> getTabelasFase(Long faseId) {
        List<Grupo> grupos = grupoRepository.findByFaseIdOrderByNomeAsc(faseId);

        return grupos.stream()
                .map(g -> faseDeGruposStrategy.calcularTabelaGrupo(g, partidaRepository.findByGrupoId(g.getId())))
                .toList();
    }
}
