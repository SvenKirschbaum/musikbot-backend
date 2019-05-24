package de.elite12.musikbot.server.api;

import java.util.Timer;
import java.util.TimerTask;

import de.elite12.musikbot.server.data.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.services.ClientService;

@RestController
@RequestMapping("/debug")
@PreAuthorize("hasRole('admin')")
public class Debug {

	@Autowired
	private ClientService client;

	@Autowired
	private ApplicationContext appContext;
	
	private static final Logger logger = LoggerFactory.getLogger(Debug.class);

	@PostMapping("{value}")
    public void doPost(@PathVariable String value) {
		switch(value) {
			case "client": {
				logger.warn(String.format("Shutting down Client by %s", ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString()));
				client.sendShutdown();
				break;
			}
			case "server": {
				logger.warn(String.format("Shutting down Server by %s", ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString()));
				new Timer().schedule(new TimerTask() {
					
					@Override
					public void run() {
						SpringApplication.exit(appContext, () -> 0);
					}
				}, 2500);
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						System.exit(0);
					}
				}, 10000);
				break;
			}
		}
    }
}
