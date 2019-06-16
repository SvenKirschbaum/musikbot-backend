package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.api.Status;
import de.elite12.musikbot.server.api.dto.StatusUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Service
@Controller
public class PushService {

    private static Logger logger = LoggerFactory.getLogger(PushService.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private Status status;

    //TODO: instead of generating the state in a ugly way here we should keep the current state in memory, mutate it on updates and publish only the diff
    public void sendState(){
        StatusUpdate state = status.getstatus();
        template.convertAndSend("/topic/state",state);
    }

    @MessageMapping("/state")
    @SendToUser("/queue/state")
    public StatusUpdate onState(Message message) {
        return status.getstatus();
    }
}
