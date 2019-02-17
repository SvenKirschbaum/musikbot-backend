package de.elite12.musikbot.server.api;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CookieFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		responseContext.getHeaders().remove("set-cookie");
	}

}
