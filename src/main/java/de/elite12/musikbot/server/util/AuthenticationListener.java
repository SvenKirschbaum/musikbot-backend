package de.elite12.musikbot.server.util;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import de.elite12.musikbot.server.data.UserMessage;
import de.elite12.musikbot.server.services.MessageService;

@Component
public class AuthenticationListener {

	@Autowired
	MessageService messages;
	
	@Autowired
	HttpServletRequest request;
	
	private static Logger logger = LoggerFactory.getLogger(AuthenticationListener.class);

	@EventListener
	public void onAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
		messages.addMessage("Login Erfolgreich", UserMessage.TYPE_SUCCESS);
		logger.info("Login from User: " + event.getAuthentication().getName() + ", IP: " + request.getRemoteAddr());
	}
	
	@EventListener
	public void onAuthenticationSuccessEvent(AuthenticationFailureBadCredentialsEvent event) {
		messages.addMessage("Nutzername/Passwort ungültig", UserMessage.TYPE_ERROR);
		logger.info("Login Fehler from User: " + event.getAuthentication().getName() + ", IP: " + request.getRemoteAddr());
	}

}
