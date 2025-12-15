package oficial.cbpitu.dto.edicao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CriarEdicaoDTO {
    
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 2021, message = "Ano deve ser a partir de 2021")
    private Integer ano;
    
    @Min(value = 1, message = "Número da edição deve ser no mínimo 1")
    private Integer numeroEdicao = 1;
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String descricao;
}
