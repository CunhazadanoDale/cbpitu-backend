package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.repository.TimeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeService {

    private final TimeRepository timeRepository;

    public List<Time> listarTodosOsTimes() {
        return timeRepository.findAll();
    }

    public Time criarTime(Time time) {
        return timeRepository.save(time);
    }

    public void deletarTime(Long id) {

        Optional<Time> time = timeRepository.findById(id);

        if (time.isPresent()) {
            Time timeTeu = time.stream().findFirst().get();

            timeRepository.delete(timeTeu);
        }

    }

    public void alterarTime(Long id, Time timeAlterado) {

        Optional<Time> timeParaAlterar = timeRepository.findById(id);

        if (timeParaAlterar.isPresent()) {
            Time timeNovo = timeParaAlterar.get();

            // timeNovo.setId(id);
            timeNovo.setTrofeus(timeAlterado.getTrofeus());
            timeNovo.setCampeonatosParticipados(timeAlterado.getCampeonatosParticipados());
            timeNovo.setCapitao(timeAlterado.getCapitao());
            timeNovo.setJogadores(timeAlterado.getJogadores());

            timeRepository.save(timeNovo);
        }
    }
}
