package de.elite12.musikbot.server.util;

import javax.servlet.http.HttpSession;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.model.User;

public class SessionHelper {
	public static User getUserFromSession(HttpSession session) {
		if(session.getAttribute("userid") == null) {
			return null;
		}
		else {
			return Controller.getInstance().getUserservice().getUserbyId((Integer)session.getAttribute("userid"));
		}
	}
	public static void attachUserToSession(HttpSession session, User user) {
		session.setAttribute("userid", user.getId());
		String token = Controller.getInstance().getUserservice().addToken(user);
		session.setAttribute("token", token);
	}
	
	public static void removeUserFromSession(HttpSession session) {
		String token = (String) session.getAttribute("token");
		if(token != null) {
			Controller.getInstance().getUserservice().removeToken(token);
		}
		session.removeAttribute("userid");
		session.removeAttribute("token");
	}
}
