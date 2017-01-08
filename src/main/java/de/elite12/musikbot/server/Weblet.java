package de.elite12.musikbot.server;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class Weblet extends HttpServlet {

    private static final long serialVersionUID = -4574458516914107420L;
    private Controller control;

    public Weblet(Controller ctr) {
        this.control = ctr;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleGuest(req);
        if (req.getRequestURI().startsWith("/register/")) {
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("control", this.getControl());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (req.getParameter("sh") != null
                ? req.getParameter("sh").equals("c135bd34e9efbeba3786900c90b5aaef79e85cec0ad68c23d48ddb1cc3") : false) {
            System.exit(0);
        }
        if (req.getParameter("sh") != null
                ? req.getParameter("sh").equals("68c1d83dec2e27c86b23cc501d800225ed689ab36b9ff0983e269b6310") : false) {
            this.control.getConnectionListener().getHandle().sendShutdown();
            resp.setContentType("text/plain");
            resp.setStatus(200);
            return;
        }
        if (req.getRequestURI().startsWith("/setstyle/")) {
            try {
                String style = req.getRequestURI().substring(10);
                if (this.getClass().getClassLoader()
                        .getResource("de/elite12/musikbot/server/resources/styles/" + style + ".css") != null) {
                    Cookie c = new Cookie("style", style);
                    c.setMaxAge(Integer.MAX_VALUE);
                    c.setPath("/");
                    resp.addCookie(c);
                }
                resp.sendRedirect(req.getHeader("referer") != null ? req.getHeader("referer") : "/");
            } catch (IndexOutOfBoundsException e) {
                Logger.getLogger(this.getClass()).debug("Ändern des Styles fehlgeschlagen", e);
            }
            return;
        }
        if (req.getRequestURI().startsWith("/impressum/")) {
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("control", this.getControl());
            req.getRequestDispatcher("/impressum.jsp").forward(req, resp);
            return;
        }
        if (req.getRequestURI().equals("/sitemap.xml")) {
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("control", this.getControl());
            req.getRequestDispatcher("/sitemap.jsp").forward(req, resp);
            return;
        }
        if (req.getRequestURI().startsWith("/statistik/")) {
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("control", this.getControl());
            PreparedStatement stmnt = null;
            ResultSet rs;
            try {
                stmnt = this.getControl().getDB().prepareStatement(
                        "SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR != 'Automatisch' GROUP BY SONG_NAME,SONG_LINK ORDER BY COUNT(*) DESC");
                rs = stmnt.executeQuery();
                req.setAttribute("mostplayed", rs);
                stmnt.close();
                stmnt = this.getControl().getDB().prepareStatement(
                        "SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE SONG_SKIPPED = TRUE AND AUTOR != 'Automatisch' GROUP BY SONG_NAME,SONG_LINK ORDER BY COUNT(*) DESC");
                rs = stmnt.executeQuery();
                req.setAttribute("mostskipped", rs);
                stmnt.close();
                stmnt = this.getControl().getDB().prepareStatement(
                        "SELECT AUTOR,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR != 'Automatisch' GROUP BY AUTOR ORDER BY COUNT(*) DESC");
                rs = stmnt.executeQuery();
                req.setAttribute("topusers", rs);
                stmnt.close();
                stmnt = this.getControl().getDB().prepareStatement(
                        "select count(*) from USER UNION ALL select count(*) from USER WHERE ADMIN = TRUE UNION ALL SELECT Count(*) FROM (SELECT AUTOR FROM PLAYLIST WHERE CHAR_LENGTH(AUTOR) = 36 GROUP BY Autor) UNION ALL select count(*) from PLAYLIST WHERE AUTOR != 'Automatisch' UNION ALL select count(*) from PLAYLIST WHERE SONG_SKIPPED = TRUE UNION ALL select sum(SONG_DAUER) from PLAYLIST WHERE SONG_SKIPPED = FALSE;");
                rs = stmnt.executeQuery();
                req.setAttribute("allgemein", rs);
                stmnt.close();
            } catch (SQLException e) {
                Logger.getLogger(this.getClass()).error("SQLException", e);
            } finally {
                try {
                    stmnt.close();
                } catch (SQLException | NullPointerException e) {
                    Logger.getLogger(this.getClass()).debug("Cant close Statement", e);
                }
            }
            req.getRequestDispatcher("/statistik.jsp").forward(req, resp);
            return;
        }
        if (req.getRequestURI().equals("/archiv/")) {
            req.setAttribute("worked", Boolean.valueOf(true));
            PreparedStatement stmnt = null;
            ResultSet rs = null;
            try {
                int p;
                try {
                    p = Integer.parseInt(req.getParameter("p"));
                    if (p < 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    p = 1;
                }
                stmnt = this.getControl().getDB().prepareStatement("SELECT COUNT(*) FROM PLAYLIST");
                rs = stmnt.executeQuery();
                rs.next();
                req.setAttribute("seiten", Double.valueOf(Math.ceil(1D * rs.getInt(1) / 30)).intValue());
                req.setAttribute("page", p);
                stmnt.close();
                stmnt = this.getControl().getDB().prepareStatement(
                        "select * from PLAYLIST WHERE SONG_PLAYED = TRUE ORDER BY SONG_SORT DESC LIMIT " + (p - 1) * 30
                                + ",30");
                rs = stmnt.executeQuery();
                req.setAttribute("result", rs);
                req.setAttribute("control", this.getControl());
                req.getRequestDispatcher("/archiv.jsp").forward(req, resp);
                stmnt.close();
            } catch (SQLException e) {
                Logger.getLogger(this.getClass()).error("SQLException", e);
            } finally {
                try {
                    stmnt.close();
                } catch (SQLException | NullPointerException e) {
                    Logger.getLogger(this.getClass()).debug("Cant close Statement", e);
                }
            }
            return;
        }

        if (!req.getRequestURI().equals("/")) {
            resp.sendError(404);
            return;
        }
        req.setAttribute("worked", Boolean.valueOf(true));
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = this.getControl().getDB()
                    .prepareStatement("select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC");
            rs = stmnt.executeQuery();
            req.setAttribute("result", rs);
            req.setAttribute("control", this.getControl());
            if (req.getHeader("User-Agent") != null && req.getHeader("User-Agent").indexOf("Mobile") != -1) {
                req.getRequestDispatcher("/mobile.jsp").forward(req, resp);
            } else {
                req.getRequestDispatcher("/home.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQLException", e);
        } finally {
            try {
                rs.close();
            } catch (NullPointerException | SQLException e) {
                Logger.getLogger(this.getClass()).error("Error closing Resultset", e);
            }
            try {
                stmnt.close();
            } catch (NullPointerException | SQLException e) {
                Logger.getLogger(this.getClass()).error("Error closing Statement", e);
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleGuest(req);
        if (req.getRequestURI().equals("/register/")) {
            if (req.getParameter("username") == null || req.getParameter("mail") == null
                    || req.getParameter("password") == null || req.getParameter("username").equals("")
                    || req.getParameter("password").equals("") || req.getParameter("mail").equals("")
                    || req.getParameter("datenschutz") == null) {
                this.getControl().addmessage(req, "Bitte fülle alle Felder aus!", UserMessage.TYPE_ERROR);
                resp.sendRedirect("/register/");
            } else {
                PreparedStatement stmnt = null;
                try {
                    User u = this.getControl().getUserservice().getUserbyName(req.getParameter("username"));
                    if (u != null) {
                        this.getControl().addmessage(req, "Dieser Benutzername wird bereits verwendet!",
                                UserMessage.TYPE_ERROR);
                        resp.sendRedirect("/register/");
                    } else {
                        u = this.getControl().getUserservice().getUserbyMail(req.getParameter("mail"));
                        if (u != null) {
                            this.getControl().addmessage(req, "Diese Email wird bereits verwendet!",
                                    UserMessage.TYPE_ERROR);
                            resp.sendRedirect("/register/");
                        } else {
                            if (!this.isValidEmailAddress(req.getParameter("mail"))) {
                                this.getControl().addmessage(req, "Die eingegebene Email-Adresse ist ung�ltig!",
                                        UserMessage.TYPE_ERROR);
                                resp.sendRedirect("/register/");
                            } else {
                                if (!req.getParameter("password").equals(req.getParameter("password2"))) {
                                    this.getControl().addmessage(req, "Passwort falsch!", UserMessage.TYPE_ERROR);
                                    resp.sendRedirect("/register/");
                                } else {
                                    if (req.getParameter("password").length() <= 3) {
                                        this.getControl().addmessage(req,
                                                "Dein Passwort muss aus mindestens 4 Zeichen bestehen!",
                                                UserMessage.TYPE_ERROR);
                                        resp.sendRedirect("/register/");
                                    } else {
                                        u = new User(req.getParameter("username"),
                                                this.getControl().MD5(req.getParameter("password")),
                                                req.getParameter("mail"), false);
                                        if (this.getControl().getUserservice().changeUser(u) == 1) {
                                            this.getControl().addmessage(req, "Registrierung Erfolgreich",
                                                    UserMessage.TYPE_SUCCESS);
                                            stmnt = this.getControl().getDB()
                                                    .prepareStatement("UPDATE PLAYLIST SET AUTOR = ? WHERE AUTOR = ?");
                                            stmnt.setString(1, u.getName());
                                            stmnt.setString(2,
                                                    ((UUID) req.getSession().getAttribute("guest_id")).toString());
                                            stmnt.executeUpdate();
                                            req.getSession().setAttribute("user", u);
                                            resp.sendRedirect("/");
                                            Logger.getLogger(this.getClass()).debug("Succesfull registration");
                                        } else {
                                            Logger.getLogger(this.getClass()).error("Unknown Error");
                                            resp.sendRedirect("/register/");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    Logger.getLogger(this.getClass()).error("SQLException", e);
                } finally {
                    try {
                        stmnt.close();
                    } catch (NullPointerException | SQLException e) {
                        Logger.getLogger(this.getClass()).debug("Eror closing Statement", e);
                    }
                }
            }
            return;
        }
        if (req.getParameter("action") == null) {
            resp.sendError(400);
            return;
        }
        switch (req.getParameter("action").toLowerCase(Locale.GERMANY)) {
        case "login": {
            if (req.getParameter("user") != null && req.getParameter("password") != null) {
                PreparedStatement stmnt = null;
                try {
                    User user = this.getControl().getUserservice().getUserbyName(req.getParameter("user"));
                    if (user != null) {
                        if (user.getPassword().equals(this.getControl().MD5(req.getParameter("password")))) {
                            this.getControl().addmessage(req, "Login Erfolgreich", UserMessage.TYPE_SUCCESS);
                            if (req.getSession().getAttribute("guest_id") != null) {
                                stmnt = this.getControl().getDB()
                                        .prepareStatement("UPDATE PLAYLIST SET AUTOR = ? WHERE AUTOR = ?");
                                stmnt.setString(1, user.getName());
                                stmnt.setString(2, ((UUID) req.getSession().getAttribute("guest_id")).toString());
                                stmnt.executeUpdate();
                            }

                            req.getSession().setAttribute("user", user);
                            resp.sendRedirect("/");
                            Logger.getLogger(this.getClass())
                                    .info("Login by User: " + user + " from IP: " + req.getHeader("X-Real-IP")
                                            + " Old-ID:" + req.getSession().getAttribute("guest_id"));

                            req.getSession().removeAttribute("guest_id");
                            return;
                        } else {
                            this.getControl().addmessage(req, "Passwort falsch!", UserMessage.TYPE_ERROR);
                        }
                    } else {
                        this.getControl().addmessage(req, "Nutzername ungültig!", UserMessage.TYPE_ERROR);
                    }
                } catch (SQLException e) {
                    Logger.getLogger(this.getClass()).error("SQLException", e);
                } finally {
                    try {
                        if (stmnt != null) {
                            stmnt.close();
                        }
                    } catch (NullPointerException | SQLException e) {
                        Logger.getLogger(this.getClass()).error("Error closing Statement", e);
                    }
                }
            }
            break;
        }
        case "logout": {
            Logger.getLogger(this.getClass()).info("Logout from User: " + req.getSession().getAttribute("user"));
            req.getSession().removeAttribute("user");
            resp.sendRedirect("/");
            return;
        }
        default: {
            break;
        }
        }
        if (req.getParameter("silent") == null) {
            resp.sendRedirect("/");
        }
    }

    private Controller getControl() {
        return control;
    }

    private boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    private void updatelastseen(User u) {
        Logger.getLogger(this.getClass()).debug("Update Lastsen Info for User: " + u);
        PreparedStatement stmnt = null;
        try {
            stmnt = this.getControl().getDB().prepareStatement("UPDATE USER SET LASTSEEN = UNIX_TIMESTAMP() WHERE NAME = ?");
            stmnt.setString(1, u.getName());
            stmnt.execute();
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQLException", e);
        } finally {
            try {
                stmnt.close();
            } catch (NullPointerException | SQLException e) {
                Logger.getLogger(this.getClass()).error("Error closing Statement", e);
            }
        }
    }

    private void handleGuest(HttpServletRequest req) {
        Logger.getLogger(this.getClass()).debug("Handling Guest...");
        UUID id = (UUID) req.getSession().getAttribute("guest_id");
        if (id == null && req.getSession().getAttribute("user") == null) {
            UUID u = UUID.randomUUID();
            Logger.getLogger(this.getClass())
                    .info("New Guest: IP: " + req.getHeader("X-Real-IP") + " Assigned ID: " + u);
            req.getSession().setAttribute("guest_id", u);
        }
        if (req.getSession().getAttribute("user") != null) {
            this.updatelastseen((User) req.getSession().getAttribute("user"));
        }
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.NotSerializableException(getClass().getName());
    }
}
