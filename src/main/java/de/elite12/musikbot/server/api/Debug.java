package de.elite12.musikbot.server.api;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.services.ClientService;

@RestController
@RequestMapping("/api/debug")
@PreAuthorize("hasRole('admin')")
public class Debug {
	
	@Autowired
	ClientService client;
	
	@Autowired
    private ApplicationContext appContext;
	
	private static Logger logger = LoggerFactory.getLogger(Debug.class);
	
	@PostMapping("{value}")
    public void doPost(@PathVariable String value) {
		switch(value) {
			case "client": {
				logger.warn("Shutting down Client");
				client.sendShutdown();
				break;
			}
			case "server": {
				logger.warn("Shutting down Server");
				new Timer().schedule(new TimerTask() {
					
					@Override
					public void run() {
						SpringApplication.exit(appContext, () -> {return 0;});
					}
				}, 2500);
				break;
			}
		}
    }
}