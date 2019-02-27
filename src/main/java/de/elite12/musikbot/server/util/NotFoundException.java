package de.elite12.musikbot.server.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason="Not found")
public class NotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8393205516274227560L;

}
