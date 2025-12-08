package oficial.cbpitu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.CriarTimeDTO;
import oficial.cbpitu.dto.TimeDTO;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.mapper.TimeMapper;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.service.TimeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/times")
@RequiredArgsConstructor
public class TimeController {

    private final TimeService timeService;
    private final TimeMapper timeMapper;

    @GetMapping
    public ResponseEntity<List<TimeDTO>> listarTodos() {
        return ResponseEntity.ok(
                timeMapper.toDTOList(timeService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeDTO> buscarPorId(@PathVariable Long id) {
        Time time = timeService.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Time", id));
        return ResponseEntity.ok(timeMapper.toDTO(time));
    }

    @PostMapping
    public ResponseEntity<TimeDTO> criar(@Valid @RequestBody CriarTimeDTO dto) {
        Time criado = timeService.criar(
                dto.getNomeTime(),
                dto.getTrofeus(),
                dto.getCapitaoId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeMapper.toDTO(criado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CriarTimeDTO dto) {
        Time atualizado = timeService.atualizar(
                id,
                dto.getNomeTime(),
                dto.getTrofeus(),
                dto.getCapitaoId());
        return ResponseEntity.ok(timeMapper.toDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        timeService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Gerenciamento de jogadores

    @PostMapping("/{timeId}/jogadores/{jogadorId}")
    public ResponseEntity<TimeDTO> adicionarJogador(
            @PathVariable Long timeId,
            @PathVariable Long jogadorId) {
        Time time = timeService.adicionarJogador(timeId, jogadorId);
        return ResponseEntity.ok(timeMapper.toDTO(time));
    }

    @DeleteMapping("/{timeId}/jogadores/{jogadorId}")
    public ResponseEntity<TimeDTO> removerJogador(
            @PathVariable Long timeId,
            @PathVariable Long jogadorId) {
        Time time = timeService.removerJogador(timeId, jogadorId);
        return ResponseEntity.ok(timeMapper.toDTO(time));
    }

    @PutMapping("/{timeId}/capitao/{jogadorId}")
    public ResponseEntity<TimeDTO> definirCapitao(
            @PathVariable Long timeId,
            @PathVariable Long jogadorId) {
        Time time = timeService.definirCapitao(timeId, jogadorId);
        return ResponseEntity.ok(timeMapper.toDTO(time));
    }
}