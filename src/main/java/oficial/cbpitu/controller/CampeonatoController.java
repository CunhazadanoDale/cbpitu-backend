package oficial.cbpitu.controller;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.*;
import oficial.cbpitu.mapper.CampeonatoMapper;
import oficial.cbpitu.mapper.ClassificacaoMapper;
import oficial.cbpitu.mapper.FaseMapper;
import oficial.cbpitu.model.Campeonato;
import oficial.cbpitu.model.Fase;
import oficial.cbpitu.model.enums.FormatoCompeticao;
import oficial.cbpitu.service.CampeonatoService;
import oficial.cbpitu.service.ClassificacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campeonatos")
@RequiredArgsConstructor
public class CampeonatoController {

    private final CampeonatoService campeonatoService;
    private final ClassificacaoService classificacaoService;
    private final CampeonatoMapper campeonatoMapper;
    private final FaseMapper faseMapper;
    private final ClassificacaoMapper classificacaoMapper;

    // CRUD

    @GetMapping
    public ResponseEntity<List<CampeonatoDTO>> listarTodos() {
        List<CampeonatoDTO> campeonatos = campeonatoMapper.toDTOList(
                campeonatoService.listarTodos());
        return ResponseEntity.ok(campeonatos);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<CampeonatoDTO>> listarAtivos() {
        List<CampeonatoDTO> campeonatos = campeonatoMapper.toDTOList(
                campeonatoService.listarAtivos());
        return ResponseEntity.ok(campeonatos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampeonatoDTO> buscarPorId(@PathVariable Long id) {
        return campeonatoService.buscarPorId(id)
                .map(campeonatoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CampeonatoDTO> criar(@RequestBody CriarCampeonatoDTO dto) {
        Campeonato campeonato = campeonatoMapper.toEntity(dto);
        Campeonato criado = campeonatoService.criar(campeonato);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campeonatoMapper.toDTO(criado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        campeonatoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Status

    @PostMapping("/{id}/abrir-inscricoes")
    public ResponseEntity<CampeonatoDTO> abrirInscricoes(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.abrirInscricoes(id);
        return ResponseEntity.ok(campeonatoMapper.toDTO(campeonato));
    }

    @PostMapping("/{id}/fechar-inscricoes")
    public ResponseEntity<CampeonatoDTO> fecharInscricoes(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.fecharInscricoes(id);
        return ResponseEntity.ok(campeonatoMapper.toDTO(campeonato));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<CampeonatoDTO> iniciar(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.iniciarCampeonato(id);
        return ResponseEntity.ok(campeonatoMapper.toDTO(campeonato));
    }

    @PostMapping("/{id}/avancar-fase")
    public ResponseEntity<CampeonatoDTO> avancarFase(@PathVariable Long id) {
        Campeonato campeonato = campeonatoService.avancarParaProximaFase(id);
        return ResponseEntity.ok(campeonatoMapper.toDTO(campeonato));
    }

    // Times

    @PostMapping("/{campeonatoId}/times/{timeId}")
    public ResponseEntity<CampeonatoDTO> inscreverTime(
            @PathVariable Long campeonatoId,
            @PathVariable Long timeId) {
        Campeonato campeonato = campeonatoService.inscreverTime(campeonatoId, timeId);
        return ResponseEntity.ok(campeonatoMapper.toDTO(campeonato));
    }

    @DeleteMapping("/{campeonatoId}/times/{timeId}")
    public ResponseEntity<CampeonatoDTO> removerTime(
            @PathVariable Long campeonatoId,
            @PathVariable Long timeId) {
        Campeonato campeonato = campeonatoService.removerTime(campeonatoId, timeId);
        return ResponseEntity.ok(campeonatoMapper.toDTO(campeonato));
    }

    // Fases

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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(faseMapper.toDTO(fase));
    }

    @GetMapping("/{id}/validar/{formato}")
    public ResponseEntity<String> validarFormato(
            @PathVariable Long id,
            @PathVariable FormatoCompeticao formato) {
        String mensagem = campeonatoService.validarConfiguracao(id, formato);
        return ResponseEntity.ok(mensagem);
    }

    // Classificacao

    @GetMapping("/grupos/{grupoId}/classificacao")
    public ResponseEntity<List<ClassificacaoDTO>> getClassificacaoGrupo(@PathVariable Long grupoId) {
        var tabela = classificacaoService.getTabelaGrupo(grupoId);
        return ResponseEntity.ok(classificacaoMapper.toDTOList(tabela));
    }
}
