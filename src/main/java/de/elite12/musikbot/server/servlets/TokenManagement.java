package de.elite12.musikbot.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.model.User;
import de.elite12.musikbot.server.util.SessionHelper;

public class TokenManagement extends HttpServlet {

	private static final long serialVersionUID = -7848616985961760876L;
	private Controller control;

	public TokenManagement(Controller ctr) {
		this.control = ctr;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Weblet.handleGuest(req);

		User u = SessionHelper.getUserFromSession(req.getSession());

		if (u == null) {
			resp.sendError(403);
			return;
		}

		req.setAttribute("token", this.control.getUserservice().getExternalToken(u));
		req.setAttribute("worked", Boolean.valueOf(true));
		req.setAttribute("control", this.control);

		req.getRequestDispatcher("/tokens.jsp").forward(req, resp);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User u = SessionHelper.getUserFromSession(req.getSession());

		if (u == null) {
			resp.sendError(403);
			return;
		}
		
		this.control.getUserservice().resetExternalToken(u);
		
		resp.sendRedirect("/tokens/");
	}
}
