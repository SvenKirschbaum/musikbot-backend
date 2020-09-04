package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.services.ClientService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Timer;
import java.util.TimerTask;

@RestController
@RequestMapping("/debug")
@PreAuthorize("hasRole('admin')")
public class Debug {

	@Autowired
	private ClientService client;

	@Autowired
	private ApplicationContext appContext;
	
	private static final Logger logger = LoggerFactory.getLogger(Debug.class);

	@PostMapping("/client")
	@ApiOperation(value = "Shutdown the Client Application", notes = "Sends a Shutdown Request to the Client. Requires Admin Permissions.")
	public void doClient() {
		logger.warn(String.format("Shutting down Client by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
		client.sendShutdown();
	}

	@PostMapping("/server")
	@ApiOperation(value = "Shutdown the Server Application", notes = "Shutdown the Server. Requires Admin Permissions.")
	public void doServer() {
		logger.warn(String.format("Shutting down Server by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
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
	}
}
