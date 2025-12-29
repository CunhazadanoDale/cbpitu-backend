package oficial.cbpitu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.CriarJogadorDTO;
import oficial.cbpitu.dto.JogadorDTO;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.mapper.JogadorMapper;
import oficial.cbpitu.model.Jogador;
import oficial.cbpitu.service.JogadorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jogadores")
@RequiredArgsConstructor
public class JogadorController {

    private final JogadorService jogadorService;
    private final JogadorMapper jogadorMapper;

    @GetMapping
    public ResponseEntity<List<JogadorDTO>> listarTodos() {
        return ResponseEntity.ok(
                jogadorMapper.toDTOList(jogadorService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JogadorDTO> buscarPorId(@PathVariable Long id) {
        Jogador jogador = jogadorService.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Jogador", id));
        return ResponseEntity.ok(jogadorMapper.toDTO(jogador));
    }
    
    @GetMapping("/{id}/detalhes")
    public ResponseEntity<oficial.cbpitu.dto.JogadorDetalhesDTO> buscarDetalhes(@PathVariable Long id) {
        return ResponseEntity.ok(jogadorService.buscarDetalhes(id));
    }

    @GetMapping("/lane/{lane}")
    public ResponseEntity<List<JogadorDTO>> buscarPorLane(@PathVariable String lane) {
        return ResponseEntity.ok(
                jogadorMapper.toDTOList(jogadorService.buscarPorLane(lane)));
    }

    @GetMapping("/sem-time")
    public ResponseEntity<List<JogadorDTO>> buscarSemTime() {
        return ResponseEntity.ok(
                jogadorMapper.toDTOList(jogadorService.buscarSemTime()));
    }

    @PostMapping
    public ResponseEntity<JogadorDTO> criar(@Valid @RequestBody CriarJogadorDTO dto) {
        Jogador jogador = jogadorMapper.toEntity(dto);
        Jogador criado = jogadorService.criar(jogador);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jogadorMapper.toDTO(criado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JogadorDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CriarJogadorDTO dto) {
        Jogador dados = jogadorMapper.toEntity(dto);
        Jogador atualizado = jogadorService.atualizar(id, dados);
        return ResponseEntity.ok(jogadorMapper.toDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        jogadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
