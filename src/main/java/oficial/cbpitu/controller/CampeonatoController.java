package oficial.cbpitu.controller;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.*;
import oficial.cbpitu.dto.campeonato.*;
import oficial.cbpitu.model.*;
import oficial.cbpitu.model.enums.FormatoCompeticao;
import oficial.cbpitu.service.CampeonatoService;
import oficial.cbpitu.service.ClassificacaoService;
import oficial.cbpitu.service.strategy.FaseDeGruposStrategy.ClassificacaoGrupo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campeonatos")
@RequiredArgsConstructor
public class CampeonatoController {

    private final CampeonatoService campeonatoService;
    private final ClassificacaoService classificacaoService;

    // ==================== CRUD ====================

    @GetMapping
    public ResponseEntity<List<CampeonatoDTO>> listarTodos() {
        List<CampeonatoDTO> campeonatos = campeonatoService.listarTodos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(campeonatos);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<CampeonatoDTO>> listarAtivos() {
        List<CampeonatoDTO> campeonatos = campeonatoService.listarAtivos()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(campeonatos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampeonatoDTO> buscarPorId(@PathVariable Long id) {
        return campeonatoService.buscarPorId(id)
                .map(c -> ResponseEntity.ok(toDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CampeonatoDTO> criar(@RequestBody CriarCampeonatoDTO dto) {
        Campeonato campeonato = new Campeonato();
        campeonato.setNome(dto.getNome());
        campeonato.setDescricao(dto.getDescricao());
        campeonato.setDataInicio(dto.getDataInicio());
        campeonato.setDataFim(dto.getDataFim());
        campeonato.setLimiteMaximoTimes(dto.getLimiteMaximoTimes());

        Campeonato criado = campeonatoService.criar(campeonato);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(criado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        campeonatoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Status ====================

    @PostMapping("/{id}/abrir-inscricoes")
    public ResponseEntity<CampeonatoDTO> abrirInscricoes(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.abrirInscricoes(id);
        return ResponseEntity.ok(toDTO(campeonato));
    }

    @PostMapping("/{id}/fechar-inscricoes")
    public ResponseEntity<CampeonatoDTO> fecharInscricoes(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.fecharInscricoes(id);
        return ResponseEntity.ok(toDTO(campeonato));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<CampeonatoDTO> iniciar(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.iniciarCampeonato(id);
        return ResponseEntity.ok(toDTO(campeonato));
    }

    @PostMapping("/{id}/avancar-fase")
    public ResponseEntity<CampeonatoDTO> avancarFase(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.avancarParaProximaFase(id);
        return ResponseEntity.ok(toDTO(campeonato));
    }

    // ==================== Times ====================

    @PostMapping("/{campeonatoId}/times/{timeId}")
    public ResponseEntity<CampeonatoDTO> inscreverTime(
            @PathVariable Long campeonatoId,
            @PathVariable Long timeId) {
        Campeonato campeonato = campeonatoService.inscreverTime(campeonatoId, timeId);
        return ResponseEntity.ok(toDTO(campeonato));
    }

    @DeleteMapping("/{campeonatoId}/times/{timeId}")
    public ResponseEntity<CampeonatoDTO> removerTime(
            @PathVariable Long campeonatoId,
            @PathVariable Long timeId) {
        Campeonato campeonato = campeonatoService.removerTime(campeonatoId, timeId);
        return ResponseEntity.ok(toDTO(campeonato));
    }

    // ==================== Fases ====================

    @PostMapping("/{id}/fases")
    public ResponseEntity<FaseDTO> adicionarFase(
            @PathVariable Long id,
            @RequestBody CriarFaseDTO dto) {
        Fase fase = campeonatoService.adicionarFase(
                id,
                dto.getNome(),
                dto.getFormato(),
                dto.getClassificadosNecessarios(),
                dto.getRodadasTotais());
        return ResponseEntity.status(HttpStatus.CREATED).body(toFaseDTO(fase));
    }

    @GetMapping("/{id}/validar/{formato}")
    public ResponseEntity<String> validarFormato(
            @PathVariable Long id,
            @PathVariable FormatoCompeticao formato) {
        String mensagem = campeonatoService.validarConfiguracao(id, formato);
        return ResponseEntity.ok(mensagem);
    }

    // ==================== Classificação ====================

    @GetMapping("/grupos/{grupoId}/classificacao")
    public ResponseEntity<List<ClassificacaoDTO>> getClassificacaoGrupo(@PathVariable Long grupoId) {
        List<ClassificacaoGrupo> tabela = classificacaoService.getTabelaGrupo(grupoId);
        List<ClassificacaoDTO> resultado = toClassificacaoDTO(tabela);
        return ResponseEntity.ok(resultado);
    }

    // ==================== Mapeamentos ====================

    private CampeonatoDTO toDTO(Campeonato c) {
        return CampeonatoDTO.builder()
                .id(c.getId())
                .nome(c.getNome())
                .descricao(c.getDescricao())
                .dataInicio(c.getDataInicio())
                .dataFim(c.getDataFim())
                .status(c.getStatus())
                .limiteMaximoTimes(c.getLimiteMaximoTimes())
                .numeroTimesInscritos(c.getNumeroTimesInscritos())
                .fases(c.getFases().stream().map(this::toFaseDTO).collect(Collectors.toList()))
                .campeao(c.getCampeao() != null ? toTimeResumoDTO(c.getCampeao()) : null)
                .build();
    }

    private FaseDTO toFaseDTO(Fase f) {
        return FaseDTO.builder()
                .id(f.getId())
                .nome(f.getNome())
                .ordem(f.getOrdem())
                .formato(f.getFormato())
                .classificadosNecessarios(f.getClassificadosNecessarios())
                .rodadasTotais(f.getRodadasTotais())
                .finalizada(f.getFinalizada())
                .grupos(f.getGrupos().stream().map(this::toGrupoDTO).collect(Collectors.toList()))
                .partidas(f.getPartidas().stream().map(this::toPartidaDTO).collect(Collectors.toList()))
                .build();
    }

    private GrupoDTO toGrupoDTO(Grupo g) {
        return GrupoDTO.builder()
                .id(g.getId())
                .nome(g.getNome())
                .times(g.getTimes().stream().map(this::toTimeResumoDTO).collect(Collectors.toList()))
                .build();
    }

    private PartidaDTO toPartidaDTO(Partida p) {
        return PartidaDTO.builder()
                .id(p.getId())
                .time1(p.getTime1() != null ? toTimeResumoDTO(p.getTime1()) : null)
                .time2(p.getTime2() != null ? toTimeResumoDTO(p.getTime2()) : null)
                .placarTime1(p.getPlacarTime1())
                .placarTime2(p.getPlacarTime2())
                .dataHora(p.getDataHora())
                .status(p.getStatus())
                .rodada(p.getRodada())
                .identificadorBracket(p.getIdentificadorBracket())
                .vencedor(p.getVencedor() != null ? toTimeResumoDTO(p.getVencedor()) : null)
                .nomeGrupo(p.getGrupo() != null ? p.getGrupo().getNome() : null)
                .build();
    }

    private TimeResumoDTO toTimeResumoDTO(Time t) {
        return TimeResumoDTO.builder()
                .id(t.getId())
                .nomeTime(t.getNomeTime())
                .build();
    }

    private List<ClassificacaoDTO> toClassificacaoDTO(List<ClassificacaoGrupo> tabela) {
        List<ClassificacaoDTO> resultado = new java.util.ArrayList<>();
        for (int i = 0; i < tabela.size(); i++) {
            ClassificacaoGrupo cg = tabela.get(i);
            resultado.add(ClassificacaoDTO.builder()
                    .posicao(i + 1)
                    .time(toTimeResumoDTO(cg.getTime()))
                    .jogos(cg.getJogos())
                    .vitorias(cg.getVitorias())
                    .empates(cg.getEmpates())
                    .derrotas(cg.getDerrotas())
                    .golsPro(cg.getGolsPro())
                    .golsContra(cg.getGolsContra())
                    .saldoGols(cg.getSaldoGols())
                    .pontos(cg.getPontos())
                    .build());
        }
        return resultado;
    }
}
