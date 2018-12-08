package de.elite12.musikbot.server.rest;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import de.elite12.musikbot.server.Controller;
import de.elite12.musikbot.server.User;
import de.elite12.musikbot.server.Weblet;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityContextFilter implements ContainerRequestFilter {

	public SecurityContextFilter() {
	}

	@Override
	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		String authToken = requestContext.getHeaderString("Authorization");
		User u = Controller.getInstance().getUserservice().getUserbyToken(authToken);
		
		if(u != null) {
			Weblet.updatelastseen(u);
		}
		requestContext.setSecurityContext(new SecurityContextImpl(u));
	}

}
