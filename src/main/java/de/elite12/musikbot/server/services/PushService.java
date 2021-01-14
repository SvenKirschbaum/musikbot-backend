package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.api.StatusController;
import de.elite12.musikbot.server.api.dto.StatusUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Service
@Controller
public class PushService {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private StatusController statusController;

    //TODO: instead of generating the state in a ugly way here we should keep the current state in memory, mutate it on updates and publish only the diff
    public void sendState(){
        StatusUpdate state = statusController.getStatus();
        template.convertAndSend("/topic/state",state);
    }

    /**
     * This method is called when a client subscribes to the state topic and returns the current state to the subscribing client, so the client has the most recent state available
     * @return StatusUpdate The current Status to be send back to the subscribing user.
     */
    @SubscribeMapping("/state")
    public StatusUpdate onStateSubscription() {
        return statusController.getStatus();
    }
}
