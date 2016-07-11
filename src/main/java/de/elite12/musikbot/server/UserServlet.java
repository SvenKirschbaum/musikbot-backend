package de.elite12.musikbot.server;

import javax.servlet.http.HttpServlet;

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

	private void writeObject(java.io.ObjectOutputStream stream)
			throws java.io.IOException {
		throw new java.io.NotSerializableException(getClass().getName());
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws java.io.IOException, ClassNotFoundException {
		throw new java.io.NotSerializableException(getClass().getName());
	}
}
