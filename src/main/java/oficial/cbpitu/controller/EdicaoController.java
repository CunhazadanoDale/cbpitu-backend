package oficial.cbpitu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oficial.cbpitu.dto.edicao.CriarEdicaoDTO;
import oficial.cbpitu.dto.edicao.EdicaoDTO;
import oficial.cbpitu.dto.edicao.EscalacaoDTO;
import oficial.cbpitu.service.EdicaoService;
import oficial.cbpitu.service.EscalacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edicoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EdicaoController {
    
    private final EdicaoService edicaoService;
    private final EscalacaoService escalacaoService;
    
    @GetMapping
    public ResponseEntity<List<EdicaoDTO>> listarTodas() {
        return ResponseEntity.ok(edicaoService.listarTodas());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EdicaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(edicaoService.buscarPorId(id));
    }
    
    @GetMapping("/ano/{ano}")
    public ResponseEntity<List<EdicaoDTO>> buscarPorAno(@PathVariable Integer ano) {
        return ResponseEntity.ok(edicaoService.buscarPorAno(ano));
    }
    
    @PostMapping
    public ResponseEntity<EdicaoDTO> criar(@Valid @RequestBody CriarEdicaoDTO dto) {
        EdicaoDTO edicao = edicaoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(edicao);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EdicaoDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CriarEdicaoDTO dto) {
        return ResponseEntity.ok(edicaoService.atualizar(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        edicaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    // Endpoints relacionados a escalações de uma edição
    @GetMapping("/{id}/escalacoes")
    public ResponseEntity<List<EscalacaoDTO>> buscarEscalacoesDaEdicao(@PathVariable Long id) {
        return ResponseEntity.ok(escalacaoService.buscarPorEdicao(id));
    }
}
