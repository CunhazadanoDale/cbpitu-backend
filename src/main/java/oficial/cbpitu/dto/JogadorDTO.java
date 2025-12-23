package oficial.cbpitu.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JogadorDTO {

    private Long id;
    private String nickname;
    private String nomeReal;
    private String laneLol;
    private Integer titulos;
    private List<TimeResumoDTO> times = new ArrayList<>();
}
