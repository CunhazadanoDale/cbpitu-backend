package oficial.cbpitu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.edicao.CriarEscalacaoDTO;
import oficial.cbpitu.dto.edicao.EscalacaoDTO;
import oficial.cbpitu.service.EscalacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/escalacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EscalacaoController {
    
    private final EscalacaoService escalacaoService;
    
    @GetMapping
    public ResponseEntity<List<EscalacaoDTO>> listarTodas() {
        return ResponseEntity.ok(escalacaoService.listarTodas());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EscalacaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(escalacaoService.buscarPorId(id));
    }
    
    @GetMapping("/time/{timeId}")
    public ResponseEntity<List<EscalacaoDTO>> buscarHistoricoTime(@PathVariable Long timeId) {
        return ResponseEntity.ok(escalacaoService.buscarHistoricoTime(timeId));
    }
    
    @PostMapping
    public ResponseEntity<EscalacaoDTO> criar(@Valid @RequestBody CriarEscalacaoDTO dto) {
        EscalacaoDTO escalacao = escalacaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(escalacao);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EscalacaoDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CriarEscalacaoDTO dto) {
        return ResponseEntity.ok(escalacaoService.atualizar(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        escalacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
