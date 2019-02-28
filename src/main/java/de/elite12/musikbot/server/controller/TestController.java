package de.elite12.musikbot.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.elite12.musikbot.server.data.UserMessage;
import de.elite12.musikbot.server.services.MessageService;

@Controller
public class TestController {
	
	@Autowired
	MessageService msg;

	@GetMapping("/test")
	public String testAction(Model m) {
		msg.addMessage("Testnachricht", UserMessage.TYPE_SUCCESS);
		return "test";
	}
}
