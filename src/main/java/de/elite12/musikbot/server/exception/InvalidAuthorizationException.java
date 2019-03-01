package de.elite12.musikbot.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason="Authorization Header invalid")
public class InvalidAuthorizationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1353597131050406623L;
	
	public InvalidAuthorizationException(String msg) {
		super(msg);
	}

}
