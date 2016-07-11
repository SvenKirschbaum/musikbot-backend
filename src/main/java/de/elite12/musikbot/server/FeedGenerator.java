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

public class FeedGenerator extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7467196858728632326L;
	private Controller control;

	public FeedGenerator(Controller ctr) {
		this.control = ctr;
	}

	private Controller getControl() {
		return control;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setAttribute("worked", Boolean.valueOf(true));
		ResultSet rs = null;
		PreparedStatement stmnt = null;
		try {
			Logger.getLogger(FeedGenerator.class).debug("Generation Feed...");
			stmnt = this
					.getControl()
					.getDB()
					.prepareStatement(
							"select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC");
			rs = stmnt.executeQuery();
			req.setAttribute("result", rs);
			req.setAttribute("control", this.getControl());
			resp.setContentType("text/xml");
			req.getRequestDispatcher("/feed.jsp").forward(req, resp);
		} catch (SQLException e) {
			Logger.getLogger(FeedGenerator.class).error("Got SQLException", e);
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
				Logger.getLogger(FeedGenerator.class).error(
						"Error closing ResultSet", e);
			}
			try {
				stmnt.close();
			} catch (Exception e) {
				Logger.getLogger(FeedGenerator.class).error(
						"Error closing Statement", e);
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
