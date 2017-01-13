package de.elite12.musikbot.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2212625943054480381L;
	@SuppressWarnings("unused")
	private Controller ctr;

	public UserServlet(Controller con) {
		this.ctr = con;
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		/*String[] path = req.getContextPath().split("/");
		if(path.length>0) {
			System.out.println(path[path.length-1]);
			User user = this.ctr.getUserservice().getUserbyName(path[path.length-1]);
			if(user != null) {
				req.setAttribute("user", user);
				req.setAttribute("worked", Boolean.valueOf(true));
				req.setAttribute("control", this.ctr);
				req.getRequestDispatcher("/user.jsp").forward(req, resp);
				return;
			}
		}*/
		resp.sendError(404);
		return;
	}

	private void writeObject(java.io.ObjectOutputStream stream)
			throws java.io.IOException {
		throw new java.io.NotSerializableException(getClass().getName());
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws java.io.IOException, ClassNotFoundException {
		throw new java.io.NotSerializableException(getClass().getName());
	}
}
