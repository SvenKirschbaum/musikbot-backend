package de.elite12.musikbot.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.api.services.youtube.model.Video;
import com.google.common.base.Ascii;
import com.wrapper.spotify.model_objects.specification.Track;

import de.elite12.musikbot.server.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.shared.Util;

public class SongManagement extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 8275807815188101274L;
    private Controller control;

    public SongManagement(Controller ctr) {
        this.control = ctr;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()) {
            try (
                    Connection c = this.getControl().getDB();
                    PreparedStatement stmnt = c.prepareStatement("SELECT * FROM LOCKED_SONGS");
            ) {
                Logger.getLogger(SongManagement.class).debug("Building locked Song List");
                this.getControl().addmessage(req,
                        "Hinweis: Admins sind in der Lage gesperrte Videos zur Playlist hinzuzufügen, bekommen jedoch eine Warnung angezeigt!",
                        UserMessage.TYPE_NOTIFY);
                req.setAttribute("worked", Boolean.valueOf(true));
                ResultSet rs = stmnt.executeQuery();
                req.setAttribute("result", rs);
                req.setAttribute("control", this.getControl());
                req.getRequestDispatcher("/songmanagment.jsp").forward(req, resp);
            } catch (SQLException e) {
                Logger.getLogger(SongManagement.class).error("Unknown SQL-Exception", e);
            }
        }
        if (!resp.isCommitted()) {
            resp.sendRedirect("/");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()) {
            switch (req.getParameter("action")) {
                case "add": {
                    String song = req.getParameter("song");
                    if (song != null) {
                        try (
                                Connection c = this.getControl().getDB();
                                PreparedStatement stmnt = c
                                        .prepareStatement("INSERT INTO LOCKED_SONGS (YTID, SONG_NAME) VALUES  (?, ?)");
                        ) {
                        	UnifiedTrack ut = UnifiedTrack.fromURL(song);
                            
                            stmnt.setString(1, ut.getId());
                            stmnt.setString(2, Ascii.truncate(ut.getTitle(), 350, "..."));
                            stmnt.executeUpdate();
                            Logger.getLogger(SongManagement.class).info("added Song to locklist: " + ut.getId()
                                    + "by User: " + u);
                        } catch (SQLIntegrityConstraintViolationException e) {
                            this.getControl().addmessage(req, "Dieser Song befindet sich bereits in der Liste!",
                                    UserMessage.TYPE_ERROR);
                        } catch (IOException | SQLException e) {
                            Logger.getLogger(SongManagement.class).warn("Service Exception", e);
                            this.getControl().addmessage(req, "Fehler beim Sperren des Songs",
                                    UserMessage.TYPE_ERROR);
                        } catch (TrackNotAvailableException e) {
                        	this.getControl().addmessage(req, "Der eingegebene Song existiert nicht",
                                    UserMessage.TYPE_ERROR);
						} catch (InvalidURLException e) {
							this.getControl().addmessage(req, "Die eingegebene URL ist ungültig",
                                    UserMessage.TYPE_ERROR);
						}
                    } else {
                        this.getControl().addmessage(req, "Bitte gib einen Song an!", UserMessage.TYPE_ERROR);
                    }
                    break;
                }
                case "delete": {
                    if (req.getParameterValues("song") != null) {
                        try (
                                Connection c = this.getControl().getDB();
                                PreparedStatement stmnt = c.prepareStatement("DELETE FROM LOCKED_SONGS WHERE id = ?");
                        ) {
                            for (String s : req.getParameterValues("song")) {
                                stmnt.setInt(1, Integer.parseInt(s));
                                stmnt.addBatch();
                            }
                            stmnt.executeBatch();
                            Logger.getLogger(SongManagement.class)
                                    .info("deleted Songs from locklist: "
                                            + Arrays.toString(req.getParameterValues("song")) + "by User: "
                                            + u);
                        } catch (SQLException e) {
                            Logger.getLogger(SongManagement.class).error("Sql Exception", e);
                        }
                    }
                    break;
                }
                default: {
                    break;
                }
            }
            resp.sendRedirect("/songs/");
            return;
        }
        resp.sendRedirect("/");
    }

    private Controller getControl() {
        return control;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.NotSerializableException(getClass().getName());
    }
}
