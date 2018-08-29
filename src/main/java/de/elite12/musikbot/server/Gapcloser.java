package de.elite12.musikbot.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.wrapper.spotify.models.Track;

import de.elite12.musikbot.shared.Song;
import de.elite12.musikbot.shared.Util;
import de.elite12.musikbot.shared.Util.SpotifyPlaylistHelper;

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
    private Permutationhelper permutation;

    public Gapcloser(Controller ctr) {
        this.control = ctr;
        try (
                Connection c = this.getControl().getDB();
                PreparedStatement stmnt = c.prepareStatement("SELECT value FROM SETTINGS WHERE name = ?");
                PreparedStatement stmnt2 = c.prepareStatement("SELECT value FROM SETTINGS WHERE name = ?");
        ) {
            stmnt.setString(1, "gapcloser");
            ResultSet rs = stmnt.executeQuery();
            rs.next();
            this.mode = Mode.valueOf(rs.getString("value"));
            
            stmnt2.setString(1, "playlist");
            ResultSet rs2 = stmnt2.executeQuery();
            rs2.next();
            this.playlist = rs2.getString("value");
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQLException", e);
        }
        createPermutation();
    }

    private Controller getControl() {
        return control;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()) {
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
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()
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
            //if (Util.getPID(req.getParameter("playlist")) != null || Util.getSPID(req.getParameter("playlist")) != null) {
            if (Util.getPID(req.getParameter("playlist")) != null) {
                /*String link = Util.getSPID(req.getParameter("playlist")) == null
                        ? "https://www.youtube.com/playlist?list=" + Util.getPID(req.getParameter("playlist"))
                        : Util.getSPID(req.getParameter("playlist")).toString();*/
            	String link = "https://www.youtube.com/playlist?list=" + Util.getPID(req.getParameter("playlist"));
                this.setPlaylist(link);
            }

            createPermutation();
            Logger.getLogger(this.getClass())
                    .info("Gapcloser zu " + this.getMode() + " geändert (Playlist: " + this.getPlaylist() + ")");
            save();
        }
        resp.sendRedirect("/gapcloser/");
    }

    private void save() {
        try (
                Connection c = this.getControl().getDB();
                PreparedStatement stmnt = c.prepareStatement("UPDATE SETTINGS SET value = ? WHERE name = ?");
                PreparedStatement stmnt2 = c.prepareStatement("UPDATE SETTINGS SET value = ? WHERE name = ?");
        ) {
            stmnt.setString(1, this.getMode().name());
            stmnt.setString(2, "gapcloser");
            stmnt.executeUpdate();
            Logger.getLogger(this.getClass()).debug("Einstellung " + this.getMode() + " wurde gespeichert");
            
            stmnt2.setString(1, this.getPlaylist());
            stmnt2.setString(2, "playlist");
            stmnt2.executeUpdate();
            Logger.getLogger(this.getClass()).debug("Einstellung " + this.getPlaylist() + " wurde gespeichert");
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQLException", e);
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
            try (
                    Connection c = this.getControl().getDB();
                    PreparedStatement stmnt = c.prepareStatement(
                            "INSERT INTO PLAYLIST (SONG_PLAYED, SONG_LINK, SONG_NAME, SONG_INSERT_AT, AUTOR, SONG_DAUER, SONG_SKIPPED, SONG_PLAYED_AT) VALUES(?, ?, ?, NOW(), ?, ?, FALSE, NOW())",
                            Statement.RETURN_GENERATED_KEYS);
            ) {
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
                Logger.getLogger(Gapcloser.class).error("Error inserting Song", e);
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
                try (
                        Connection c = this.getControl().getDB();
                        PreparedStatement stmnt = c.prepareStatement(
                                "SELECT SONG_NAME,SONG_LINK,SONG_DAUER FROM (SELECT SONG_NAME,SONG_LINK,SONG_DAUER FROM PLAYLIST WHERE AUTOR != 'Automatisch' GROUP BY SONG_NAME,SONG_LINK,SONG_DAUER ORDER BY COUNT(*) DESC LIMIT 100) WHERE SONG_LINK NOT LIKE '%spotify%' ORDER BY RAND() LIMIT 1");
                ) {
                    ResultSet rs = stmnt.executeQuery();
                    rs.next();
                    Song s = new Song(0, null, null, rs.getString(1), rs.getString(2), false, false, null, 0,
                            rs.getInt(3));
                    if (s.gettype().equals("youtube")) {
                        try {
                            List<Video> list = this.getControl().getYouTube().videos().list("status,contentDetails")
                                    .setKey(Controller.key).setId(Util.getVID(s.getLink()))
                                    .setFields(
                                            "items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/regionRestriction")
                                    .execute().getItems();
                            if (list != null) {
                                if (!list.get(0).getStatus().getUploadStatus().equals("processed")
                                        || list.get(0).getStatus().getUploadStatus().equals("private")) {
                                    throw new IOException("Video not available: " + s.getLink());
                                }
                                if (list.get(0).getContentDetails() != null) {
                                    if (list.get(0).getContentDetails().getRegionRestriction() != null) {
                                        if (list.get(0).getContentDetails().getRegionRestriction()
                                                .getBlocked() != null) {
                                            if (list.get(0).getContentDetails().getRegionRestriction().getBlocked()
                                                    .contains("DE")) {
                                                throw new IOException("Video not available: " + s.getLink());
                                            }
                                        }
                                        if (list.get(0).getContentDetails().getRegionRestriction()
                                                .getAllowed() != null) {
                                            if (!list.get(0).getContentDetails().getRegionRestriction().getAllowed()
                                                    .contains("DE")) {
                                                throw new IOException("Video not available: " + s.getLink());
                                            }
                                        }
                                    }
                                }
                            } else {
                                throw new IOException("Video not available: " + s.getLink());
                            }
                        } catch (IndexOutOfBoundsException | IOException e) {
                            Logger.getLogger(this.getClass()).warn("Song seems to got deleted, skipping", e);
                            return this.findnextSong();
                        }
                    } else {
                        Track t = Util.getTrack(Util.getSID(s.getLink()));
                        if (t == null) {
                            Logger.getLogger(this.getClass()).warn("Track invalid");
                            return this.findnextSong();
                        }
                    }
                    return s;
                } catch (SQLException e) {
                    Logger.getLogger(this.getClass()).error("SQLException", e);
                }
                break;
            }
            case RANDOM: {
                try (
                        Connection c = this.getControl().getDB();
                        PreparedStatement stmnt = c.prepareStatement("select * from PLAYLIST WHERE SONG_LINK NOT LIKE '%spotify%' ORDER BY RAND() LIMIT 1");
                ) {
                    ResultSet rs = stmnt.executeQuery();
                    Song s = new Song(rs);
                    if (s.gettype().equals("youtube")) {
                        try {
                            List<Video> list = this.getControl().getYouTube().videos().list("status,contentDetails")
                                    .setKey(Controller.key).setId(Util.getVID(s.getLink()))
                                    .setFields(
                                            "items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/regionRestriction")
                                    .execute().getItems();
                            if (list != null) {
                                if (!list.get(0).getStatus().getUploadStatus().equals("processed")
                                        || list.get(0).getStatus().getUploadStatus().equals("private")) {
                                    throw new IOException("Video not available: " + s.getLink());
                                }
                                if (list.get(0).getContentDetails() != null) {
                                    if (list.get(0).getContentDetails().getRegionRestriction() != null) {
                                        if (list.get(0).getContentDetails().getRegionRestriction()
                                                .getBlocked() != null) {
                                            if (list.get(0).getContentDetails().getRegionRestriction().getBlocked()
                                                    .contains("DE")) {
                                                throw new IOException("Video not available: " + s.getLink());
                                            }
                                        }
                                        if (list.get(0).getContentDetails().getRegionRestriction()
                                                .getAllowed() != null) {
                                            if (!list.get(0).getContentDetails().getRegionRestriction().getAllowed()
                                                    .contains("DE")) {
                                                throw new IOException("Video not available: " + s.getLink());
                                            }
                                        }
                                    }
                                }
                            } else {
                                throw new IOException("Video not available: " + s.getLink());
                            }
                        } catch (IndexOutOfBoundsException | IOException e) {
                            Logger.getLogger(this.getClass()).warn("Song seems to got deleted, skipping", e);
                            return this.findnextSong();
                        }
                    } else {
                        Track t = Util.getTrack(Util.getSID(s.getLink()));
                        if (t == null) {
                            Logger.getLogger(this.getClass()).warn("Track invalid");
                            return this.findnextSong();
                        }
                    }
                    return s;
                } catch (SQLException e) {
                    Logger.getLogger(this.getClass()).error("SQLException", e);
                }
                break;
            }
            case PLAYLIST: {
                String pid = Util.getPID(this.getPlaylist());
                SpotifyPlaylistHelper spid = Util.getSPID(this.getPlaylist());
                int id = this.permutation.getNext();

                if (pid != null) {
                    int page = (int) Math.floor(id / 50.0);
                    try {
                        PlaylistItemListResponse r = Controller.getInstance().getYouTube().playlistItems()
                                .list("snippet,status").setKey(Controller.key).setPlaylistId(pid).setMaxResults(50L)
                                .setFields(
                                        "items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                                .execute();
                        for (int i = 0; i < page; i++) {
                            r = Controller.getInstance().getYouTube().playlistItems().list("snippet,status")
                                    .setKey(Controller.key).setPlaylistId(pid).setMaxResults(50L)
                                    .setPageToken(r.getNextPageToken())
                                    .setFields(
                                            "items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                                    .execute();
                        }
                        PlaylistItem item = r.getItems().get(id % 50);
                        Song s = new Song(0, null, null, item.getSnippet().getTitle(),
                                "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId(),
                                false, false, null, 0, 0);

                        Video v;
                        List<Video> vlist = this.getControl().getYouTube().videos()
                                .list("status,snippet,contentDetails").setKey(Controller.key)
                                .setId(Util.getVID(s.getLink()))
                                .setFields(
                                        "items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/duration,items/snippet/categoryId,items/snippet/title,items/contentDetails/regionRestriction")
                                .execute().getItems();
                        if (vlist != null) {
                            v = vlist.get(0);
                            if (!v.getStatus().getUploadStatus().equals("processed")
                                    || v.getStatus().getUploadStatus().equals("private")) {
                                throw new IOException("Video not available: " + s.getLink());
                            }
                            if (v.getContentDetails() != null) {
                                if (v.getContentDetails().getRegionRestriction() != null) {
                                    if (v.getContentDetails().getRegionRestriction().getBlocked() != null) {
                                        if (v.getContentDetails().getRegionRestriction().getBlocked().contains("DE")) {
                                            throw new IOException("Video not available: " + s.getLink());
                                        }
                                    }
                                    if (v.getContentDetails().getRegionRestriction().getAllowed() != null) {
                                        if (!v.getContentDetails().getRegionRestriction().getAllowed().contains("DE")) {
                                            throw new IOException("Video not available: " + s.getLink());
                                        }
                                    }
                                }
                            }
                        } else {
                            throw new IOException("Video not available: " + s.getLink());
                        }

                        return s;
                    } catch (IndexOutOfBoundsException | IllegalArgumentException | IOException
                            | StackOverflowError e1) {
                        Logger.getLogger(this.getClass()).warn("Song invalid, skipping", e1);
                        return this.findnextSong();
                    }
                } else if (spid != null) {
                    Track t = Util.getTrackfromPlaylist(spid.user, spid.pid, id);
                    if (t == null) {
                        Logger.getLogger(this.getClass()).warn("Track invalid");
                        return this.findnextSong();
                    }
                    Song s = new Song(0, null, null, "[" + t.getArtists().get(0).getName() + "] " + t.getName(),
                            "https://open.spotify.com/track/" + t.getId(), false, false, null, 0, 0);
                    return s;
                } else {
                    return null;
                }

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

    private void createPermutation() {
        String pid = Util.getPID(this.getPlaylist());
        SpotifyPlaylistHelper spid = Util.getSPID(this.getPlaylist());
        if (pid != null) {
            try {
                PlaylistItemListResponse r = this.getControl().getYouTube().playlistItems().list("snippet")
                        .setKey(Controller.key).setPlaylistId(pid).setMaxResults(1L).setFields("pageInfo/totalResults")
                        .execute();
                this.permutation = new Permutationhelper(r.getPageInfo().getTotalResults());
            } catch (IOException e) {
                Logger.getLogger(Gapcloser.class).fatal("Error loading Playlist count", e);
            }
        } else if (spid != null) {
            this.permutation = new Permutationhelper(Util.getPlaylistlength(spid.user, spid.pid));
        } else {
            Logger.getLogger(Gapcloser.class).fatal("Playlist invalid");
        }
    }

    private class Permutationhelper {
        private int p;
        private List<Integer> list;

        public Permutationhelper(int size) {
            this.p = 0;
            this.list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                this.list.add(i);
            }
            Collections.shuffle(this.list);
        }

        public int getNext() {
            if (p >= this.list.size()) {
                p = 0;
                Collections.shuffle(this.list);
            }
            return this.list.get(this.p++);
        }
    }
}
