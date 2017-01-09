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
import com.wrapper.spotify.models.Track;

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
        if (req.getSession().getAttribute("user") != null && ((User) req.getSession().getAttribute("user")).isAdmin()) {
            PreparedStatement stmnt = null;
            ResultSet rs = null;
            Connection c = null;
            try {
                Logger.getLogger(SongManagement.class).debug("Building locked Song List");
                this.getControl().addmessage(req,
                        "Hinweis: Admins sind in der Lage gesperrte Videos zur Playlist hinzuzufügen, bekommen jedoch eine Warnung angezeigt!",
                        UserMessage.TYPE_NOTIFY);
                req.setAttribute("worked", Boolean.valueOf(true));

                c = this.getControl().getDB();
                stmnt = c.prepareStatement("SELECT * FROM LOCKED_SONGS");
                rs = stmnt.executeQuery();
                req.setAttribute("result", rs);
                req.setAttribute("control", this.getControl());
                req.getRequestDispatcher("/songmanagment.jsp").forward(req, resp);
            } catch (SQLException e) {
                Logger.getLogger(SongManagement.class).error("Unknown SQL-Exception", e);
            } finally {
                try {
                    rs.close();
                } catch (NullPointerException | SQLException e) {
                    Logger.getLogger(SongManagement.class).error("Unknown SQL-Exception", e);
                }
                try {
                    stmnt.close();
                } catch (NullPointerException | SQLException e) {
                    Logger.getLogger(SongManagement.class).error("Unknown SQL-Exception", e);
                }
                try {
                    c.close();
                } catch (NullPointerException | SQLException e) {
                    Logger.getLogger(SongManagement.class).error("Unknown SQL-Exception", e);
                }
            }
        }
        if (!resp.isCommitted()) {
            resp.sendRedirect("/");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("user") != null && ((User) req.getSession().getAttribute("user")).isAdmin()) {
            switch (req.getParameter("action")) {
            case "add": {
                String song = req.getParameter("song");
                if (song != null) {
                    PreparedStatement stmnt = null;
                    Connection c = null;
                    try {
                        String vid = Util.getVID(req.getParameter("song"));
                        String sid = Util.getSID(req.getParameter("song"));
                        if (vid != null) {
                            List<Video> list = this.getControl().getYouTube().videos()
                                    .list("status,snippet,contentDetails").setKey(Controller.key).setId(vid)
                                    .setFields(
                                            "items/status/uploadStatus,items/status/privacyStatus,items/snippet/title,items/contentDetails/regionRestriction")
                                    .execute().getItems();
                            Video v;
                            if (list != null) {
                                v = list.get(0);
                                if (!v.getStatus().getUploadStatus().equals("processed")
                                        || v.getStatus().getUploadStatus().equals("private")) {
                                    throw new IOException("Video not available: " + vid);
                                }
                                if (v.getContentDetails() != null) {
                                    if (v.getContentDetails().getRegionRestriction() != null) {
                                        if (v.getContentDetails().getRegionRestriction().getBlocked() != null) {
                                            if (v.getContentDetails().getRegionRestriction().getBlocked()
                                                    .contains("DE")) {
                                                throw new IOException("Video not available: " + vid);
                                            }
                                        }
                                        if (v.getContentDetails().getRegionRestriction().getAllowed() != null) {
                                            if (!v.getContentDetails().getRegionRestriction().getAllowed()
                                                    .contains("DE")) {
                                                throw new IOException("Video not available: " + vid);
                                            }
                                        }
                                    }
                                }
                            } else {
                                throw new IOException("Video not available");
                            }
                            c = this.getControl().getDB();
                            stmnt = c.prepareStatement("INSERT INTO LOCKED_SONGS (YTID, SONG_NAME) VALUES	(?, ?)");
                            stmnt.setString(1, vid);
                            stmnt.setString(2, Ascii.truncate(v.getSnippet().getTitle(), 350, "..."));
                            stmnt.executeUpdate();
                            Logger.getLogger(SongManagement.class).info("added Song to locklist: " + vid + "by User: "
                                    + req.getSession().getAttribute("user"));
                        } else if (sid != null) {
                            Track track = Util.getTrack(sid);
                            if (track == null) {
                                throw new IOException("");
                            }
                            c = this.getControl().getDB();
                            stmnt = c.prepareStatement("INSERT INTO LOCKED_SONGS (YTID, SONG_NAME) VALUES	(?, ?)");
                            stmnt.setString(1, sid);
                            stmnt.setString(2, Ascii.truncate(track.getName(), 350, "..."));
                            stmnt.executeUpdate();
                            Logger.getLogger(SongManagement.class).info("added Song to locklist: " + sid + "by User: "
                                    + req.getSession().getAttribute("user"));
                        } else {
                            throw new IOException("");
                        }
                    } catch (IOException e) {
                        Logger.getLogger(SongManagement.class).warn("Service Exception", e);
                        this.getControl().addmessage(req, "Der eingegebene Link war ungültig!", UserMessage.TYPE_ERROR);
                    } catch (SQLIntegrityConstraintViolationException e) {
                        Logger.getLogger(SongManagement.class).warn("ConstraintViolation", e);
                        this.getControl().addmessage(req, "Dieser Song befindet sich bereits in der Liste!",
                                UserMessage.TYPE_ERROR);
                    } catch (SQLException e) {
                        Logger.getLogger(SongManagement.class).error("Sql Exception", e);
                    } finally {
                    	try {
                            stmnt.close();
                        } catch (NullPointerException | SQLException e) {
                            Logger.getLogger(SongManagement.class).error("Exception closing Statement", e);
                        }
                    	try {
                            c.close();
                        } catch (NullPointerException | SQLException e) {
                            Logger.getLogger(SongManagement.class).error("Exception closing Connection", e);
                        }
                    }
                } else {
                    this.getControl().addmessage(req, "Bitte gib einen Song an!", UserMessage.TYPE_ERROR);
                }
                break;
            }
            case "delete": {
                if (req.getParameterValues("song") != null) {
                    PreparedStatement stmnt = null;
                    Connection c = null;
                    try {
                    	c = this.getControl().getDB();
                        stmnt = c.prepareStatement("DELETE FROM LOCKED_SONGS WHERE id = ?");
                        for (String s : req.getParameterValues("song")) {
                            stmnt.setInt(1, Integer.parseInt(s));
                            stmnt.addBatch();
                        }
                        stmnt.executeBatch();
                        Logger.getLogger(SongManagement.class)
                                .info("deleted Songs from locklist: " + Arrays.toString(req.getParameterValues("song"))
                                        + "by User: " + req.getSession().getAttribute("user"));
                    } catch (SQLException e) {
                        Logger.getLogger(SongManagement.class).error("Sql Exception", e);
                    } finally {
                    	try {
                            stmnt.close();
                        } catch (NullPointerException | SQLException e) {
                            Logger.getLogger(SongManagement.class).error("Exception closing Statement", e);
                        }
                    	try {
                            c.close();
                        } catch (NullPointerException | SQLException e) {
                            Logger.getLogger(SongManagement.class).error("Exception closing Connection", e);
                        }
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
