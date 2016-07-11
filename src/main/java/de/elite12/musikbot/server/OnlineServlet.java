package de.elite12.musikbot.server;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class OnlineServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 340540942974552888L;
	private Controller control;

	public OnlineServlet(Controller ctr) {
		this.control = ctr;
	}

	private Controller getControl() {
		return control;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setAttribute("worked", Boolean.valueOf(true));
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			Logger.getLogger(OnlineServlet.class).debug("Building Online List");
			stmnt = this
					.getControl()
					.getDB()
					.prepareStatement(
							"select * from USER WHERE UNIX_TIMESTAMP()-LASTSEEN <300");
			rs = stmnt.executeQuery();
			req.setAttribute("result", rs);
			req.setAttribute("control", this.getControl());
			req.getRequestDispatcher("/whoistonline.jsp").forward(req, resp);
		} catch (SQLException e) {
			Logger.getLogger(OnlineServlet.class).error(
					"Error Building Online List", e);
		} finally {
			try {
				rs.close();
			} catch (NullPointerException | SQLException e) {
				Logger.getLogger(OnlineServlet.class).error(
						"Error Closing ResultSet", e);
			}
			try {
				stmnt.close();
			} catch (NullPointerException | SQLException e) {
				Logger.getLogger(OnlineServlet.class).error(
						"Error Closing Statement", e);
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
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
