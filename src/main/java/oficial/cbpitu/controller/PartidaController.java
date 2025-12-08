package oficial.cbpitu.controller;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.*;
import oficial.cbpitu.dto.campeonato.PartidaDTO;
import oficial.cbpitu.dto.campeonato.ResultadoDTO;
import oficial.cbpitu.model.*;
import oficial.cbpitu.service.PartidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/partidas")
@RequiredArgsConstructor
public class PartidaController {

    private final PartidaService partidaService;

    // ==================== Listagem ====================

    @GetMapping
    public ResponseEntity<List<PartidaDTO>> listarTodas() {
        List<PartidaDTO> partidas = partidaService.listarTodas()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(partidas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaDTO> buscarPorId(@PathVariable Long id) {
        return partidaService.buscarPorId(id)
                .map(p -> ResponseEntity.ok(toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/campeonato/{campeonatoId}")
    public ResponseEntity<List<PartidaDTO>> listarPorCampeonato(@PathVariable Long campeonatoId) {
        List<PartidaDTO> partidas = partidaService.listarPorCampeonato(campeonatoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(partidas);
    }

    @GetMapping("/fase/{faseId}")
    public ResponseEntity<List<PartidaDTO>> listarPorFase(@PathVariable Long faseId) {
        List<PartidaDTO> partidas = partidaService.listarPorFase(faseId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(partidas);
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<PartidaDTO>> listarPorGrupo(@PathVariable Long grupoId) {
        List<PartidaDTO> partidas = partidaService.listarPorGrupo(grupoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(partidas);
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<PartidaDTO>> listarPorTime(@PathVariable Long timeId) {
        List<PartidaDTO> partidas = partidaService.listarPorTime(timeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(partidas);
    }

    @GetMapping("/campeonato/{campeonatoId}/pendentes")
    public ResponseEntity<List<PartidaDTO>> listarPendentes(@PathVariable Long campeonatoId) {
        List<PartidaDTO> partidas = partidaService.listarPendentesPorCampeonato(campeonatoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(partidas);
    }

    @GetMapping("/campeonato/{campeonatoId}/proximas")
    public ResponseEntity<List<PartidaDTO>> listarProximas(
            @PathVariable Long campeonatoId,
            @RequestParam(defaultValue = "5") int limite) {
        List<PartidaDTO> partidas = partidaService.buscarProximasPartidas(campeonatoId, limite)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(partidas);
    }

    // ==================== Agendamento ====================

    @PutMapping("/{id}/agendar")
    public ResponseEntity<PartidaDTO> agendar(
            @PathVariable Long id,
            @RequestParam LocalDateTime dataHora) {
        Partida partida = partidaService.agendarPartida(id, dataHora);
        return ResponseEntity.ok(toDTO(partida));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<PartidaDTO> iniciar(@PathVariable Long id) {
        Partida partida = partidaService.iniciarPartida(id);
        return ResponseEntity.ok(toDTO(partida));
    }

    // ==================== Resultado ====================

    @PutMapping("/{id}/resultado")
    public ResponseEntity<PartidaDTO> registrarResultado(
            @PathVariable Long id,
            @RequestBody ResultadoDTO resultado) {
        Partida partida = partidaService.registrarResultado(
                id,
                resultado.getPlacarTime1(),
                resultado.getPlacarTime2());
        return ResponseEntity.ok(toDTO(partida));
    }

    @PutMapping("/{id}/resultado-serie")
    public ResponseEntity<PartidaDTO> registrarResultadoSerie(
            @PathVariable Long id,
            @RequestBody ResultadoDTO resultado) {
        Partida partida = partidaService.registrarResultadoSerie(
                id,
                resultado.getPlacarTime1(),
                resultado.getPlacarTime2());
        return ResponseEntity.ok(toDTO(partida));
    }

    @PutMapping("/{id}/wo/{timeVencedorId}")
    public ResponseEntity<PartidaDTO> registrarWO(
            @PathVariable Long id,
            @PathVariable Long timeVencedorId) {
        Partida partida = partidaService.registrarWO(id, timeVencedorId);
        return ResponseEntity.ok(toDTO(partida));
    }

    @PutMapping("/{id}/corrigir")
    public ResponseEntity<PartidaDTO> corrigirResultado(
            @PathVariable Long id,
            @RequestBody ResultadoDTO resultado) {
        Partida partida = partidaService.corrigirResultado(
                id,
                resultado.getPlacarTime1(),
                resultado.getPlacarTime2());
        return ResponseEntity.ok(toDTO(partida));
    }

    // ==================== Status ====================

    @PostMapping("/{id}/adiar")
    public ResponseEntity<PartidaDTO> adiar(@PathVariable Long id) {
        Partida partida = partidaService.adiarPartida(id);
        return ResponseEntity.ok(toDTO(partida));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PartidaDTO> cancelar(@PathVariable Long id) {
        Partida partida = partidaService.cancelarPartida(id);
        return ResponseEntity.ok(toDTO(partida));
    }

    // ==================== Mapeamento ====================

    private PartidaDTO toDTO(Partida p) {
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
}
