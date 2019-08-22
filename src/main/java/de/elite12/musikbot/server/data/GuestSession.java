package de.elite12.musikbot.server.data;

import java.io.Serializable;
import java.util.UUID;

import lombok.ToString;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Getter;

@SessionScope
@Component
@Getter
@ToString
public class GuestSession implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3315137645660113279L;
	private String id;
	
	public GuestSession() {
		id = UUID.randomUUID().toString();
	}
}
