package de.elite12.musikbot.server.data;

import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.UUID;

@SessionScope
@Component
@Getter
@ToString
public class GuestSession implements Serializable {
	private static final long serialVersionUID = 3315137645660113279L;

	private static Logger logger = LoggerFactory.getLogger(GuestSession.class);

	private String id;
	
	public GuestSession(HttpServletRequest httpServletRequest)
	{
		id = UUID.randomUUID().toString();
		logger.info(String.format("Guest-Session create from %s: %s", httpServletRequest.getRemoteAddr(), this));
	}
}
