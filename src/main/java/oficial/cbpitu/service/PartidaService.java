package oficial.cbpitu.service;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.exception.OperacaoInvalidaException;
import oficial.cbpitu.exception.RecursoNaoEncontradoException;
import oficial.cbpitu.exception.RegraNegocioException;
import oficial.cbpitu.model.Partida;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.model.enums.StatusPartida;
import oficial.cbpitu.repository.PartidaRepository;
import oficial.cbpitu.repository.TimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final TimeRepository timeRepository;
    private final CampeonatoService campeonatoService;

    public List<Partida> listarTodas() {
        return partidaRepository.findAll();
    }

    public Optional<Partida> buscarPorId(Long id) {
        return partidaRepository.findById(id);
    }

    public List<Partida> listarPorCampeonato(Long campeonatoId) {
        return partidaRepository.findByCampeonatoId(campeonatoId);
    }

    public List<Partida> listarPorFase(Long faseId) {
        return partidaRepository.findByFaseId(faseId);
    }

    public List<Partida> listarPorGrupo(Long grupoId) {
        return partidaRepository.findByGrupoId(grupoId);
    }

    public List<Partida> listarPorTime(Long timeId) {
        return partidaRepository.findByTimeId(timeId);
    }

    public List<Partida> listarPendentesPorCampeonato(Long campeonatoId) {
        return partidaRepository.findByCampeonatoIdAndStatus(campeonatoId, StatusPartida.PENDENTE);
    }

    // Agendamento

    @Transactional
    public Partida agendarPartida(Long partidaId, LocalDateTime dataHora) {
        Partida partida = buscarOuFalhar(partidaId);

        if (partida.isFinalizada()) {
            throw new OperacaoInvalidaException("Não é possível agendar uma partida já finalizada");
        }

        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Data/hora de agendamento deve ser no futuro");
        }

        partida.setDataHora(dataHora);
        partida.setStatus(StatusPartida.AGENDADA);

        return partidaRepository.save(partida);
    }

    @Transactional
    public Partida iniciarPartida(Long partidaId) {
        Partida partida = buscarOuFalhar(partidaId);

        if (partida.isFinalizada()) {
            throw new OperacaoInvalidaException("Partida já foi finalizada");
        }

        partida.setStatus(StatusPartida.EM_ANDAMENTO);
        return partidaRepository.save(partida);
    }

    // Registro de resultado

    @Transactional
    public Partida registrarResultado(Long partidaId, int placarTime1, int placarTime2) {
        Partida partida = buscarOuFalhar(partidaId);

        if (partida.isFinalizada()) {
            throw new OperacaoInvalidaException("Partida já foi finalizada. Use a opção de corrigir resultado.");
        }

        partida.registrarResultado(placarTime1, placarTime2);

        Partida salva = partidaRepository.save(partida);
        campeonatoService.verificarEProcessarAvanco(salva.getFase().getId(), salva);

        return salva;
    }

    @Transactional
    public Partida registrarResultadoSerie(Long partidaId, int vitoriasTime1, int vitoriasTime2) {
        Partida partida = buscarOuFalhar(partidaId);

        if (partida.isFinalizada()) {
            throw new OperacaoInvalidaException("Série já foi finalizada");
        }

        partida.registrarResultadoSerie(vitoriasTime1, vitoriasTime2);

        Partida salva = partidaRepository.save(partida);
        campeonatoService.verificarEProcessarAvanco(salva.getFase().getId(), salva);

        return salva;
    }

    @Transactional
    public Partida registrarWO(Long partidaId, Long timeVencedorId) {
        Partida partida = buscarOuFalhar(partidaId);
        Time vencedor = timeRepository.findById(timeVencedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Time", timeVencedorId));

        if (!vencedor.equals(partida.getTime1()) && !vencedor.equals(partida.getTime2())) {
            throw new RegraNegocioException("Time informado não participa desta partida");
        }

        partida.setVencedor(vencedor);
        partida.setStatus(StatusPartida.WO);

        // Define placar simbólico
        if (vencedor.equals(partida.getTime1())) {
            partida.setPlacarTime1(3);
            partida.setPlacarTime2(0);
        } else {
            partida.setPlacarTime1(0);
            partida.setPlacarTime2(3);
        }

        Partida salva = partidaRepository.save(partida);
        campeonatoService.verificarEProcessarAvanco(salva.getFase().getId(), salva);

        return salva;
    }

    // Alterações de status

    @Transactional
    public Partida cancelarPartida(Long partidaId) {
        Partida partida = buscarOuFalhar(partidaId);

        if (partida.isFinalizada()) {
            throw new OperacaoInvalidaException("Não é possível cancelar uma partida já finalizada");
        }

        partida.setStatus(StatusPartida.CANCELADA);
        return partidaRepository.save(partida);
    }

    @Transactional
    public Partida adiarPartida(Long partidaId) {
        Partida partida = buscarOuFalhar(partidaId);

        if (partida.isFinalizada()) {
            throw new OperacaoInvalidaException("Não é possível adiar uma partida já finalizada");
        }

        partida.setStatus(StatusPartida.ADIADA);
        partida.setDataHora(null);
        return partidaRepository.save(partida);
    }

    @Transactional
    public Partida corrigirResultado(Long partidaId, int novoPlacarTime1, int novoPlacarTime2) {
        Partida partida = buscarOuFalhar(partidaId);

        partida.setPlacarTime1(novoPlacarTime1);
        partida.setPlacarTime2(novoPlacarTime2);

        // Recalcula vencedor
        if (novoPlacarTime1 > novoPlacarTime2) {
            partida.setVencedor(partida.getTime1());
        } else if (novoPlacarTime2 > novoPlacarTime1) {
            partida.setVencedor(partida.getTime2());
        } else {
            partida.setVencedor(null);
        }

        Partida salva = partidaRepository.save(partida);
        campeonatoService.verificarEProcessarAvanco(salva.getFase().getId(), salva);

        return salva;
    }

    // Consultas

    public List<Partida> buscarPartidasDeHoje(Long campeonatoId) {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimDia = inicioDia.plusDays(1);

        return partidaRepository.findByCampeonatoId(campeonatoId).stream()
                .filter(p -> p.getDataHora() != null)
                .filter(p -> !p.getDataHora().isBefore(inicioDia) && p.getDataHora().isBefore(fimDia))
                .toList();
    }

    public List<Partida> buscarProximasPartidas(Long campeonatoId, int limite) {
        LocalDateTime agora = LocalDateTime.now();

        return partidaRepository.findByCampeonatoIdAndStatus(campeonatoId, StatusPartida.AGENDADA)
                .stream()
                .filter(p -> p.getDataHora() != null && p.getDataHora().isAfter(agora))
                .sorted((a, b) -> a.getDataHora().compareTo(b.getDataHora()))
                .limit(limite)
                .toList();
    }

    // Helper

    private Partida buscarOuFalhar(Long id) {
        return partidaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Partida", id));
    }
}
