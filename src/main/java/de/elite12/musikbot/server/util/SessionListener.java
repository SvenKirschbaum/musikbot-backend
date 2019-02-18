package de.elite12.musikbot.server.util;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import de.elite12.musikbot.server.core.Controller;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		String token = (String) se.getSession().getAttribute("token");
		if(token != null) {
			Controller.getInstance().getUserservice().removeToken(token);
		}
	}

}
