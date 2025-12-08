package oficial.cbpitu.controller;

import lombok.RequiredArgsConstructor;
import oficial.cbpitu.model.Time;
import oficial.cbpitu.service.TimeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class TimeController {

    private final TimeService timeService;

    @GetMapping("/listarTodosTimes")
    public List<Time> llistarOsTimes() {
        return timeService.listarTodosOsTimes();
    }

    @PostMapping("/criarTime")
    public Time criandoTime(@RequestBody Time times) {
        return timeService.criarTime(times);
    }

    @PutMapping("/alterarTime/{id}")
    public void alterarTime(@PathVariable Long id, @RequestBody Time time) {
        timeService.alterarTime(id, time);
    }

    @DeleteMapping("/deletar/{id}")
    public void deletarTime(@PathVariable Long id) {
        timeService.deletarTime(id);
    }
}