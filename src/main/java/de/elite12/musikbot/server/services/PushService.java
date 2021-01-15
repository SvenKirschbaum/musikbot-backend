package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.api.dto.StatusUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PushService {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private StateService stateService;

    public void sendState(){
        StatusUpdate state = stateService.getStateUpdate();
        template.convertAndSend("/topic/state",state);
    }
}
