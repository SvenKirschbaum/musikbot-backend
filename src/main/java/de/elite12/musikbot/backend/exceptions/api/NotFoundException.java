package de.elite12.musikbot.backend.exceptions.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason="Not found")
public class NotFoundException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 8393205516274227560L;

}
