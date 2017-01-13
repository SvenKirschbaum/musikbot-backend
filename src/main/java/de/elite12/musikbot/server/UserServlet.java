package de.elite12.musikbot.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import de.elite12.musikbot.server.Weblet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.Weblet.TopEntry;

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
		String[] path = req.getPathInfo().split("/");
		if (path.length > 0) {
			User user = this.ctr.getUserservice().getUserbyName(path[path.length - 1]);
			if (user != null) {
				req.setAttribute("viewuser", user);
				req.setAttribute("worked", Boolean.valueOf(true));
				req.setAttribute("control", this.ctr);
				boolean admin = (((User) req.getSession().getAttribute("user")) != null
						? ((User) req.getSession().getAttribute("user")).isAdmin() : false);
				ArrayList<DataEntry> userinfo = new ArrayList<>();
				userinfo.add(new DataEntry("ID:", user.getId().toString(), false));
				userinfo.add(new DataEntry("Username:", user.getName(), admin));
				if (user.equals(req.getSession().getAttribute("user")) || admin) {
					userinfo.add(new DataEntry("Email:", user.getEmail(), true));
				}
				userinfo.add(new DataEntry("Admin: ", user.isAdmin() ? "Ja" : "Nein", admin));

				try (
						Connection c = this.ctr.getDB();
						PreparedStatement stmnt = c.prepareStatement("SELECT COUNT(SONG_ID) FROM PLAYLIST WHERE AUTOR = ?");
						PreparedStatement stmnt2 = c.prepareStatement("SELECT COUNT(SONG_ID) FROM PLAYLIST WHERE AUTOR = ? AND SONG_SKIPPED = TRUE");
						PreparedStatement stmnt3 = c.prepareStatement("SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR = ? GROUP BY SONG_LINK ORDER BY COUNT(*) DESC LIMIT 10");
						PreparedStatement stmnt4 = c.prepareStatement("SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR = ? AND SONG_SKIPPED = TRUE GROUP BY SONG_LINK ORDER BY COUNT(*) DESC LIMIT 10");
				) {
					stmnt.setString(1, user.getName());
					ResultSet r = stmnt.executeQuery();
					r.next();
					userinfo.add(new DataEntry("Wünsche:", new Integer(r.getInt(1)).toString(), false));
					stmnt2.setString(1, user.getName());
					ResultSet r2 = stmnt2.executeQuery();
					r2.next();
					userinfo.add(new DataEntry("Davon übersprungen:", new Integer(r2.getInt(1)).toString(), false));
					stmnt3.setString(1, user.getName());
					ResultSet r3 = stmnt3.executeQuery();
					ArrayList<TopEntry> list = new ArrayList<>(10);
					while (r3.next()) {
	                    list.add(new TopEntry(r3.getString(1), r3.getString(2), r3.getInt(3)));
	                }
					req.setAttribute("toplist", list);
					stmnt4.setString(1, user.getName());
					ResultSet r4 = stmnt4.executeQuery();
					list = new ArrayList<>(10);
					while (r4.next()) {
	                    list.add(new TopEntry(r4.getString(1), r4.getString(2), r4.getInt(3)));
	                }
					req.setAttribute("topskipped", list);
				} catch (SQLException e) {
					Logger.getLogger(UserServlet.class).error("Error reading Userdata",e);
					resp.sendError(500);
					return;
				}

				req.setAttribute("userinfo", userinfo.toArray(new DataEntry[0]));
				req.getRequestDispatcher("/user.jsp").forward(req, resp);
				return;
			} else {
				resp.sendError(404);
			}
		} else {
			resp.sendError(404);
		}
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
		throw new java.io.NotSerializableException(getClass().getName());
	}

	private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
		throw new java.io.NotSerializableException(getClass().getName());
	}

	public class DataEntry {
		public DataEntry(String name, String value, boolean change) {
			this.name = name;
			this.value = value;
			this.changeable = change;
		}

		public String name;
		public String value;
		public boolean changeable;
	}
}
