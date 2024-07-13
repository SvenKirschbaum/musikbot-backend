package de.elite12.musikbot.backend.api;

import de.elite12.musikbot.backend.api.dto.ErrorDTO;
import de.elite12.musikbot.backend.exceptions.api.BadRequestException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class MessageExceptionHandlerAdvise {

    @MessageExceptionHandler
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ErrorDTO handleException(BadRequestException e) {
        ErrorDTO errorDTO = new ErrorDTO();

        errorDTO.setMessage(e.getMessage());

        return errorDTO;
    }

    @MessageExceptionHandler
    @SendToUser(value = "/queue/errors", broadcast = false)
    public ErrorDTO handleException(RuntimeException ignoredE) {
        ErrorDTO errorDTO = new ErrorDTO();

        errorDTO.setMessage("Es ist ein Fehler aufgetreten");

        return errorDTO;
    }
}
