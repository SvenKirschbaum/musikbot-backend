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
import com.wrapper.spotify.model_objects.specification.Track;

import de.elite12.musikbot.server.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.shared.Song;
import de.elite12.musikbot.shared.Util;
import de.elite12.musikbot.shared.Util.SpotifyPlaylistHelper;
import javassist.bytecode.stackmap.BasicBlock.Catch;

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
            if (Util.getPID(req.getParameter("playlist")) != null
                    || Util.getSPID(req.getParameter("playlist")) != null) {
                String link = Util.getSPID(req.getParameter("playlist")) == null
                        ? "https://www.youtube.com/playlist?list=" + Util.getPID(req.getParameter("playlist"))
                        : Util.getSPID(req.getParameter("playlist")).toString();
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
        try (
    		Connection c = this.getControl().getDB();
            PreparedStatement insertstmnt = c.prepareStatement(
                "INSERT INTO PLAYLIST (SONG_PLAYED, SONG_LINK, SONG_NAME, SONG_INSERT_AT, AUTOR, SONG_DAUER, SONG_SKIPPED, SONG_PLAYED_AT) VALUES(?, ?, ?, NOW(), ?, ?, FALSE, NOW())",
                Statement.RETURN_GENERATED_KEYS
            );
        ) {
        	for(int i = 0; i < 3; i++) {
        		String url = selectCandidate();
        		if(url == null) {
    				return null;
    			}
        		
        		UnifiedTrack ut;
        		
        		try {
        			ut = UnifiedTrack.fromURL(url);
        		}
        		catch(TrackNotAvailableException | InvalidURLException e) {
        			Logger.getLogger(this.getClass()).debug("Generated invalid Song",e);
        			continue;
        		}
        		
    			insertstmnt.setBoolean(1, true);
    			insertstmnt.setString(2, ut.getLink());
    			insertstmnt.setString(3, ut.getTitle());
    			insertstmnt.setString(4, "Automatisch");
    			insertstmnt.setInt(5, ut.getDuration());
    			insertstmnt.executeUpdate();
    			
                ResultSet key = insertstmnt.getGeneratedKeys();
                key.next();
                
                Song s = new Song(0, null, "Automatisch", ut.getTitle(), ut.getLink(), true, false, null, (int) key.getLong(1), (int) ut.getDuration());
                
                Logger.getLogger(this.getClass())
                    .info("Gapcloser generated Song (ID: " + key.getLong(1) + ")" + s.toString());
                
                return s;
        	}
        	
        	Logger.getLogger(this.getClass()).error("Loading Song Failed three times");
        	return null;
        } catch (SQLException | IOException e) {
            Logger.getLogger(this.getClass()).error("Error loading Gapcloser Song", e);
        }
        return null;
    }
    
    private String selectCandidate() throws SQLException, IOException {
    	try (
                Connection c = this.getControl().getDB();
                PreparedStatement randomstmnt = c.prepareStatement("select SONG_LINK from PLAYLIST ORDER BY RAND() LIMIT 1");
    			PreparedStatement random100stmnt = c.prepareStatement(
                    "SELECT SONG_NAME,SONG_LINK,SONG_DAUER FROM (SELECT SONG_LINK FROM PLAYLIST WHERE AUTOR != 'Automatisch' GROUP BY SONG_LINK ORDER BY COUNT(*) DESC LIMIT 100) ORDER BY RAND() LIMIT 1"
    			);
        ) {
	    	switch (this.getMode()) {
				case OFF: {
					return null;
				}
				case RANDOM: {
                    ResultSet rs = randomstmnt.executeQuery();
                    rs.next();
                    return rs.getString("SONG_LINK");
				}
				case RANDOM100: {
					ResultSet rs = random100stmnt.executeQuery();
                    rs.next();
                    return rs.getString("SONG_LINK");
				}
				case PLAYLIST: {
					String pid = Util.getPID(this.getPlaylist());
	                SpotifyPlaylistHelper spid = Util.getSPID(this.getPlaylist());
	                int id = this.permutation.getNext();

	                if (pid != null) {
	                    int page = (int) Math.floor(id / 50.0);
                        PlaylistItemListResponse r = Controller.getInstance().getYouTube().playlistItems()
                            .list("snippet,status").setKey(Controller.key).setPlaylistId(pid).setMaxResults(50L)
                            .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                            .execute();
                        for (int i = 0; i < page; i++) {
                            r = Controller.getInstance().getYouTube().playlistItems().list("snippet,status")
                                .setKey(Controller.key).setPlaylistId(pid).setMaxResults(50L)
                                .setPageToken(r.getNextPageToken())
                                .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                                .execute();
                        }
                        PlaylistItem item = r.getItems().get(id % 50);
                        return "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId();
	                } else if (spid != null) {
	                    Track t = Util.getTrackfromPlaylist(spid.user, spid.pid, id);
	                    return "https://open.spotify.com/track/" + t.getId();
	                }
				}
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
