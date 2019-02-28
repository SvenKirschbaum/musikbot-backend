package de.elite12.musikbot.server.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import de.elite12.musikbot.server.core.MusikbotServiceProperties;

@ControllerAdvice
public class CommonModel {
	
	@Autowired
	private MusikbotServiceProperties config;

	@ModelAttribute("version")
	public String getVersion() {
		return config.getVersion();
	}
}
