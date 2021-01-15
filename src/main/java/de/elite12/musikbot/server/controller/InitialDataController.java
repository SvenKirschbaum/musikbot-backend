package de.elite12.musikbot.server.controller;

import de.elite12.musikbot.server.api.dto.StatusUpdate;
import de.elite12.musikbot.server.services.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class InitialDataController {

    @Autowired
    private StateService stateService;

    /**
     * This method is called when a client subscribes to the state topic and returns the current state to the subscribing client, so the client has the most recent state available
     * @return StatusUpdate The current Status to be send back to the subscribing user.
     */
    @SubscribeMapping("/state")
    public StatusUpdate onStateSubscription() {
        return stateService.getStateUpdate();
    }
}
