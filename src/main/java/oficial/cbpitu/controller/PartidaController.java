package oficial.cbpitu.controller;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.campeonato.PartidaDTO;
import oficial.cbpitu.dto.campeonato.ResultadoDTO;
import oficial.cbpitu.mapper.PartidaMapper;
import oficial.cbpitu.model.Partida;
import oficial.cbpitu.service.PartidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/partidas")
@RequiredArgsConstructor
public class PartidaController {

    private final PartidaService partidaService;
    private final PartidaMapper partidaMapper;

    // Listagem

    @GetMapping
    public ResponseEntity<List<PartidaDTO>> listarTodas() {
        return ResponseEntity.ok(
                partidaMapper.toDTOList(partidaService.listarTodas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaDTO> buscarPorId(@PathVariable Long id) {
        return partidaService.buscarPorId(id)
                .map(partidaMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/campeonato/{campeonatoId}")
    public ResponseEntity<List<PartidaDTO>> listarPorCampeonato(@PathVariable Long campeonatoId) {
        return ResponseEntity.ok(
                partidaMapper.toDTOList(partidaService.listarPorCampeonato(campeonatoId)));
    }

    @GetMapping("/fase/{faseId}")
    public ResponseEntity<List<PartidaDTO>> listarPorFase(@PathVariable Long faseId) {
        return ResponseEntity.ok(
                partidaMapper.toDTOList(partidaService.listarPorFase(faseId)));
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<PartidaDTO>> listarPorGrupo(@PathVariable Long grupoId) {
        return ResponseEntity.ok(
                partidaMapper.toDTOList(partidaService.listarPorGrupo(grupoId)));
    }

    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<PartidaDTO>> listarPorTime(@PathVariable Long timeId) {
        return ResponseEntity.ok(
                partidaMapper.toDTOList(partidaService.listarPorTime(timeId)));
    }

    @GetMapping("/campeonato/{campeonatoId}/pendentes")
    public ResponseEntity<List<PartidaDTO>> listarPendentes(@PathVariable Long campeonatoId) {
        return ResponseEntity.ok(
                partidaMapper.toDTOList(partidaService.listarPendentesPorCampeonato(campeonatoId)));
    }

    @GetMapping("/campeonato/{campeonatoId}/proximas")
    public ResponseEntity<List<PartidaDTO>> listarProximas(
            @PathVariable Long campeonatoId,
            @RequestParam(defaultValue = "5") int limite) {
        return ResponseEntity.ok(
                partidaMapper.toDTOList(partidaService.buscarProximasPartidas(campeonatoId, limite)));
    }

    // Agendamento

    @PutMapping("/{id}/agendar")
    public ResponseEntity<PartidaDTO> agendar(
            @PathVariable Long id,
            @RequestParam LocalDateTime dataHora) {
        Partida partida = partidaService.agendarPartida(id, dataHora);
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<PartidaDTO> iniciar(@PathVariable Long id) {
        Partida partida = partidaService.iniciarPartida(id);
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }

    // Resultado

    @PutMapping("/{id}/resultado")
    public ResponseEntity<PartidaDTO> registrarResultado(
            @PathVariable Long id,
            @RequestBody ResultadoDTO resultado) {
        Partida partida = partidaService.registrarResultado(
                id,
                resultado.getPlacarTime1(),
                resultado.getPlacarTime2());
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }

    @PutMapping("/{id}/resultado-serie")
    public ResponseEntity<PartidaDTO> registrarResultadoSerie(
            @PathVariable Long id,
            @RequestBody ResultadoDTO resultado) {
        Partida partida = partidaService.registrarResultadoSerie(
                id,
                resultado.getPlacarTime1(),
                resultado.getPlacarTime2());
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }

    @PutMapping("/{id}/wo/{timeVencedorId}")
    public ResponseEntity<PartidaDTO> registrarWO(
            @PathVariable Long id,
            @PathVariable Long timeVencedorId) {
        Partida partida = partidaService.registrarWO(id, timeVencedorId);
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }

    @PutMapping("/{id}/corrigir")
    public ResponseEntity<PartidaDTO> corrigirResultado(
            @PathVariable Long id,
            @RequestBody ResultadoDTO resultado) {
        Partida partida = partidaService.corrigirResultado(
                id,
                resultado.getPlacarTime1(),
                resultado.getPlacarTime2());
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }

    // Status

    @PostMapping("/{id}/adiar")
    public ResponseEntity<PartidaDTO> adiar(@PathVariable Long id) {
        Partida partida = partidaService.adiarPartida(id);
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PartidaDTO> cancelar(@PathVariable Long id) {
        Partida partida = partidaService.cancelarPartida(id);
        return ResponseEntity.ok(partidaMapper.toDTO(partida));
    }
}
