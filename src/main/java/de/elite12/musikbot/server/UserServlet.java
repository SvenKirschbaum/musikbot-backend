package de.elite12.musikbot.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

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
        Weblet.handleGuest(req);
        String[] path = req.getPathInfo().split("/");
        if (path.length > 0) {
            User user = this.ctr.getUserservice().getUserbyName(path[path.length - 1]);
            boolean guest = false;
            try {
                user = new User(UUID.fromString(path[path.length - 1]).toString(), null, "gast@elite12.de", false);
                guest = true;
            } catch (IllegalArgumentException e) {

            }
            if (user != null) {
                req.setAttribute("viewuser", user);
                req.setAttribute("worked", Boolean.valueOf(true));
                req.setAttribute("control", this.ctr);
                User u = SessionHelper.getUserFromSession(req.getSession());
                boolean admin = u != null ? u.isAdmin() : false;
                ArrayList<DataEntry> userinfo = new ArrayList<>();
                if (!guest) {
                    userinfo.add(
                            new DataEntry("ID:", user.getId() != null ? user.getId().toString() : "Null", false, "id"));
                }
                userinfo.add(new DataEntry("Username:", user.getName(), admin, "username"));
                if ((user.equals(u) || admin) && !guest) {
                    userinfo.add(new DataEntry("Email:", user.getEmail(), true, "email"));
                    userinfo.add(new DataEntry("Passwort:", "****", true, "password"));
                }
                userinfo.add(new DataEntry("Admin: ", user.isAdmin() ? "Ja" : "Nein", admin && !guest, "admin"));

                try (
                        Connection c = this.ctr.getDB();
                        PreparedStatement stmnt = c
                                .prepareStatement("SELECT COUNT(SONG_ID) FROM PLAYLIST WHERE AUTOR = ?");
                        PreparedStatement stmnt2 = c.prepareStatement(
                                "SELECT COUNT(SONG_ID) FROM PLAYLIST WHERE AUTOR = ? AND SONG_SKIPPED = TRUE");
                        PreparedStatement stmnt3 = c.prepareStatement(
                                "SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR = ? GROUP BY SONG_LINK ORDER BY COUNT(*) DESC LIMIT 10");
                        PreparedStatement stmnt4 = c.prepareStatement(
                                "SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR = ? AND SONG_SKIPPED = TRUE GROUP BY SONG_LINK ORDER BY COUNT(*) DESC LIMIT 10");
                        PreparedStatement stmnt5 = c.prepareStatement(
                                "SELECT SONG_NAME,SONG_LINK,SONG_ID FROM PLAYLIST WHERE AUTOR = ? ORDER BY SONG_ID DESC LIMIT 10");
                        PreparedStatement stmnt6 = c.prepareStatement("SELECT LASTSEEN FROM USER WHERE ID = ?");
                ) {
                	if (!guest) {
	                	stmnt6.setInt(1, user.getId());
	                    ResultSet r6 = stmnt6.executeQuery();
	                    r6.next();
	                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                    userinfo.add(new DataEntry("Zuletzt aktiv:", f.format(new Date(r6.getLong(1) * 1000L)), false, ""));
                	}

                    stmnt.setString(1, user.getName());
                    ResultSet r = stmnt.executeQuery();
                    r.next();
                    userinfo.add(new DataEntry("Wünsche:", new Integer(r.getInt(1)).toString(), false, ""));
                    stmnt2.setString(1, user.getName());
                    ResultSet r2 = stmnt2.executeQuery();
                    r2.next();
                    userinfo.add(new DataEntry("Davon übersprungen:", new Integer(r2.getInt(1)).toString(), false, ""));
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
                    stmnt5.setString(1, user.getName());
                    ResultSet r5 = stmnt5.executeQuery();
                    list = new ArrayList<>(10);
                    while (r5.next()) {
                        list.add(new TopEntry(r5.getString(1), r5.getString(2), r5.getInt(3)));
                    }
                    req.setAttribute("recent", list);
                } catch (SQLException e) {
                    Logger.getLogger(UserServlet.class).error("Error reading Userdata", e);
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
        public DataEntry(String name, String value, boolean change, String urlname) {
            this.name = name;
            this.value = value;
            this.changeable = change;
            this.urlname = urlname;
        }

        public String name;
        public String urlname;
        public String value;
        public boolean changeable;
    }
}
