package de.elite12.musikbot.server;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;

import de.elite12.musikbot.shared.Song;
import de.elite12.musikbot.shared.Util;

public class Gapcloser extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 64953031560116883L;

    public enum Mode {
        OFF,
        RANDOM100,
        RANDOM,
        PLAYLIST
    };

    private Mode mode;
    private Controller control;
    private String playlist;
    private int delay = 5;

    public Gapcloser(Controller ctr) {
        this.control = ctr;
        PreparedStatement stmnt = null;
        ResultSet rs;
        try {
            stmnt = this.getControl().getDB().prepareStatement("SELECT value FROM SETTINGS WHERE name = ?");
            stmnt.setString(1, "gapcloser");
            rs = stmnt.executeQuery();
            rs.next();
            this.mode = Mode.valueOf(rs.getString("value"));
            stmnt.close();
            stmnt = this.getControl().getDB().prepareStatement("SELECT value FROM SETTINGS WHERE name = ?");
            stmnt.setString(1, "playlist");
            rs = stmnt.executeQuery();
            rs.next();
            this.playlist = rs.getString("value");
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQLException", e);
        } finally {
            try {
                stmnt.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).debug("Cant close Statement", e);
            }
        }
    }

    private Controller getControl() {
        return control;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("user") != null && ((User) req.getSession().getAttribute("user")).isAdmin()) {
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("mode", this.getMode());
            req.setAttribute("playlist", this.getPlaylist());
            req.setAttribute("control", this.getControl());
            req.getRequestDispatcher("/gapcloser.jsp").forward(req, resp);
            return;
        }
        resp.sendRedirect("/");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("user") != null && ((User) req.getSession().getAttribute("user")).isAdmin()
                && req.getParameter("mode") != null) {
            req.setAttribute("worked", Boolean.valueOf(true));
            switch (req.getParameter("mode")) {
            case "off": {
                this.mode = Mode.OFF;
                break;
            }
            case "zufall100": {
                this.mode = Mode.RANDOM100;
                break;
            }
            case "zufall": {
                this.mode = Mode.RANDOM;
                break;
            }
            case "playlist": {
                this.mode = Mode.PLAYLIST;
                break;
            }
            }
            if (Util.getPID(req.getParameter("playlist")) != null) {
                this.setPlaylist("http://www.youtube.com/playlist?list=" + Util.getPID(req.getParameter("playlist")));
            }
            Logger.getLogger(this.getClass())
                    .info("Gapcloser zu " + this.getMode() + " geändert (Playlist: " + this.getPlaylist() + ")");
            save();
        }
        resp.sendRedirect("/gapcloser/");
    }

    private void save() {
        PreparedStatement stmnt = null;
        try {
            stmnt = this.getControl().getDB().prepareStatement("UPDATE SETTINGS SET value = ? WHERE name = ?");
            stmnt.setString(1, this.getMode().name());
            stmnt.setString(2, "gapcloser");
            stmnt.executeUpdate();
            Logger.getLogger(this.getClass()).debug("Einstellung " + this.getMode() + " wurde gespeichert");
            stmnt.close();
            stmnt = this.getControl().getDB().prepareStatement("UPDATE SETTINGS SET value = ? WHERE name = ?");
            stmnt.setString(1, this.getPlaylist());
            stmnt.setString(2, "playlist");
            stmnt.executeUpdate();
            Logger.getLogger(this.getClass()).debug("Einstellung " + this.getPlaylist() + " wurde gespeichert");
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

    public Song getnextSong() {
        Song s = null;
        try {
            s = findnextSong();
        } catch (Exception e) {
            Logger.getLogger(this.getClass()).error("Error loading Gapcloser Song", e);
        }
        if (s != null) {
            try {
                PreparedStatement stmnt = this.getControl().getDB().prepareStatement(
                        "INSERT INTO PUBLIC.PLAYLIST (SONG_PLAYED, SONG_LINK, SONG_NAME, SONG_INSERT_AT, AUTOR, SONG_DAUER, SONG_SKIPPED, SONG_PLAYED_AT) VALUES(?, ?, ?, NOW(), ?, ?, FALSE, NOW())",
                        Statement.RETURN_GENERATED_KEYS);
                stmnt.setBoolean(1, true);
                stmnt.setString(2, s.getLink());
                stmnt.setString(3, s.getTitle());
                stmnt.setString(4, "Automatisch");
                stmnt.setInt(5, s.getDauer());
                stmnt.executeUpdate();
                ResultSet key = stmnt.getGeneratedKeys();
                key.next();
                Logger.getLogger(this.getClass())
                        .info("Gapcloser generated Song (ID: " + key.getLong(1) + ")" + s.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    private Song findnextSong() {
        switch (this.getMode()) {
        case OFF: {
            return null;
        }
        case RANDOM100: {
            PreparedStatement stmnt = null;
            ResultSet rs;
            try {
                stmnt = this.getControl().getDB().prepareStatement(
                        "SELECT SONG_NAME,SONG_LINK,SONG_DAUER FROM (SELECT SONG_NAME,SONG_LINK,SONG_DAUER FROM PLAYLIST WHERE AUTOR != 'Automatisch' GROUP BY SONG_NAME,SONG_LINK,SONG_DAUER ORDER BY COUNT(*) DESC LIMIT 100) ORDER BY RAND() LIMIT 1");
                rs = stmnt.executeQuery();
                rs.next();
                Song s = new Song(0, null, null, rs.getString(1), rs.getString(2), false, false, null, 0, rs.getInt(3));
                if (s.gettype().equals("youtube")) {
                    try {
                        List<Video> list = this.getControl().getYouTube().videos().list("status").setKey(Controller.key)
                                .setId(Util.getVID(s.getLink()))
                                .setFields("items/status/uploadStatus,items/status/privacyStatus").execute().getItems();
                        if (list != null) {
                            if (!list.get(0).getStatus().getUploadStatus().equals("processed")
                                    || list.get(0).getStatus().getUploadStatus().equals("private")) {
                                throw new IOException("Video not available");
                            }
                        } else {
                            throw new IOException("Video not available");
                        }
                    } catch (IndexOutOfBoundsException | IOException e) {
                        Logger.getLogger(this.getClass()).warn("Song seems to got deleted, skipping", e);
                        return this.getnextSong();
                    }
                }
                return s;
            } catch (SQLException e) {
                Logger.getLogger(this.getClass()).error("SQLException", e);
            } finally {
                try {
                    stmnt.close();
                } catch (SQLException | NullPointerException e) {
                    Logger.getLogger(this.getClass()).debug("Cant close Statement", e);
                }
            }
            break;
        }
        case RANDOM: {
            PreparedStatement stmnt = null;
            ResultSet rs;
            try {
                stmnt = this.getControl().getDB().prepareStatement("select * from PLAYLIST ORDER BY RAND() LIMIT 1");
                rs = stmnt.executeQuery();
                Song s = new Song(rs);
                if (s.gettype().equals("youtube")) {
                    try {
                        List<Video> list = this.getControl().getYouTube().videos().list("status").setKey(Controller.key)
                                .setId(Util.getVID(s.getLink()))
                                .setFields("items/status/uploadStatus,items/status/privacyStatus").execute().getItems();
                        if (list != null) {
                            if (!list.get(0).getStatus().getUploadStatus().equals("processed")
                                    || list.get(0).getStatus().getUploadStatus().equals("private")) {
                                throw new IOException("Video not available");
                            }
                        } else {
                            throw new IOException("Video not available");
                        }
                    } catch (IndexOutOfBoundsException | IOException e) {
                        Logger.getLogger(this.getClass()).warn("Song seems to got deleted, skipping", e);
                        return this.getnextSong();
                    }
                }
                return s;
            } catch (SQLException e) {
                Logger.getLogger(this.getClass()).error("SQLException", e);
            } finally {
                try {
                    stmnt.close();
                } catch (SQLException | NullPointerException e) {
                    Logger.getLogger(this.getClass()).debug("Cant close Statement", e);
                }
            }
            break;
        }
        case PLAYLIST: {
            try {
                PlaylistItemListResponse r = this.getControl().getYouTube().playlistItems().list("snippet,status")
                        .setKey(Controller.key).setPlaylistId(Util.getPID(this.getPlaylist())).setMaxResults(50L)
                        .setFields(
                                "items/snippet/title,items/snippet/resourceId/videoId,items/status/privacyStatus,pageInfo,nextPageToken")
                        .execute();
                if (r == null || r.getItems() == null) {
                    throw new IOException("Playlist not available");
                }
                int i = new Random().nextInt(r.getPageInfo().getTotalResults());
                int page = i / 50 + 1;
                int item = i % 50;

                for (int j = 1; j < page; j++) {
                    r = this.getControl().getYouTube().playlistItems().list("snippet,status").setKey(Controller.key)
                            .setPlaylistId(Util.getPID(this.getPlaylist())).setMaxResults(50L)
                            .setFields(
                                    "items/snippet/title,items/snippet/resourceId/videoId,items/status/privacyStatus,nextPageToken")
                            .setPageToken(r.getNextPageToken()).execute();
                }

                PlaylistItem pitem = r.getItems().get(item);

                Video v;
                List<Video> vlist = this.getControl().getYouTube().videos().list("status,snippet,contentDetails")
                        .setKey(Controller.key).setId(pitem.getSnippet().getResourceId().getVideoId())
                        .setFields(
                                "items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/duration,items/snippet/categoryId,items/snippet/title,items/contentDetails/regionRestriction")
                        .execute().getItems();
                if (vlist != null) {
                    v = vlist.get(0);
                    if (!v.getStatus().getUploadStatus().equals("processed")
                            || v.getStatus().getUploadStatus().equals("private")) {
                        return this.findnextSong();
                    }
                    if (v.getContentDetails() != null) {
                        if (v.getContentDetails().getRegionRestriction() != null) {
                            if (v.getContentDetails().getRegionRestriction().getBlocked() != null) {
                                if (v.getContentDetails().getRegionRestriction().getBlocked().contains("DE")) {
                                    Logger.getLogger(Gapcloser.class)
                                            .error("Video (" + pitem.getSnippet().getResourceId().getVideoId()
                                                    + ") is blocked in Germany");
                                    return this.findnextSong();
                                }
                            }
                        }
                    }
                } else {
                    throw new IOException("Video not available");
                }

                return new Song(0, null, null, pitem.getSnippet().getTitle(),
                        "https://www.youtube.com/watch?v=" + pitem.getSnippet().getResourceId().getVideoId(), false,
                        false, null, 0, (int) Duration.parse(v.getContentDetails().getDuration()).getSeconds());
            } catch (IndexOutOfBoundsException | IllegalArgumentException | IOException | StackOverflowError e) {
                Logger.getLogger(Gapcloser.class).error("Error loading Playlist, trying again in 5 Minutes", e);
                this.setMode(Mode.OFF);
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        setMode(Mode.PLAYLIST);
                        if (Controller.getInstance().getSongtitle() == null) {
                            Controller.getInstance().getConnectionListener().getHandle().stop();
                            Controller.getInstance().getConnectionListener().getHandle().start();
                        }
                    }
                }, 300000L);
            }

            break;
        }
        default: {
            return null;
        }
        }
        return null;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }
}
