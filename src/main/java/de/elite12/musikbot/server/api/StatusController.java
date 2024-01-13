package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.api.dto.StatusUpdate;
import de.elite12.musikbot.server.services.StateUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/state")
@MessageMapping("/state")
public class StatusController {

    @Autowired
    private StateUpdateService stateUpdateService;

    @GetMapping()
    @SubscribeMapping()
    @Operation(summary = "Get the current Status")
    public StatusUpdate getStatus() {
        return stateUpdateService.getStateUpdate();
    }
}
