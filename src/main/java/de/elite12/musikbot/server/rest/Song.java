package de.elite12.musikbot.server.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import de.elite12.musikbot.server.Controller;
import de.elite12.musikbot.server.User;
import de.elite12.musikbot.shared.Util;

@Path("/songs")
public class Song {
    
    @Context
    HttpServletRequest req;
    @Context
    SecurityContext sc;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{ids : .+}")
    public de.elite12.musikbot.shared.Song[] getSong(@PathParam("ids") String sid) {
        String[] a = sid.split("/");
        de.elite12.musikbot.shared.Song[] r = new de.elite12.musikbot.shared.Song[a.length];
        try {
            for (int i = 0; i < a.length; i++) {
                int id = Integer.parseInt(a[i]);
                r[i] = getSongbyID(id);
            }
        } catch (NumberFormatException e) {
            throw new WebApplicationException(400);
        }
        return r;
    }
    
    @DELETE
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{ids : .+}")
    public List<Integer> deleteSong(@PathParam("ids") String sid, @QueryParam("lock") boolean lock) {
        String[] a = sid.split("/");
        try (
                Connection c = Controller.getInstance().getDB();
                PreparedStatement stmnt = c
                        .prepareStatement("DELETE FROM PLAYLIST WHERE SONG_ID = ? AND SONG_PLAYED = FALSE");
                PreparedStatement stmnt2 = c
                        .prepareStatement("INSERT INTO LOCKED_SONGS (YTID, SONG_NAME) VALUES (?, ?)");
        ) {
            for (String b : a) {
                int id = Integer.parseInt(b);
                stmnt.setInt(1, id);
                stmnt.addBatch();
                if (lock) {
                    de.elite12.musikbot.shared.Song song = getSongbyID(id);
                    stmnt2.setString(1, song.getLink().contains("spotify") ? Util.getSID(song.getLink())
                            : Util.getVID(song.getLink()));
                    stmnt2.setString(2, song.getTitle());
                    stmnt2.addBatch();
                }
            }
            Logger.getLogger(this.getClass()).info("Songs (" + Arrays.toString(a) + ") deleted"
                    + (lock ? " and locked " : " ") + "by User: " + sc.getUserPrincipal());
            if (lock) {
                stmnt2.executeBatch();
            }
            return Arrays.asList(ArrayUtils.toObject(stmnt.executeBatch()));
        } catch (NumberFormatException e) {
            throw new WebApplicationException(400);
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQL Exception", e);
        }
        throw new WebApplicationException(500);
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response createSong(String url) {
    	String uuid = null;
    	if(req.getSession().getAttribute("guest_id") != null) {
    		uuid = req.getSession().getAttribute("guest_id").toString();
    	}
    	
    	if(req.getHeader("User-Agent") != null && req.getHeader("User") != null) {
    		if(req.getHeader("User-Agent").equals("ContestBot") && !req.getHeader("User").isEmpty()) {
    			uuid = UUID.nameUUIDFromBytes(req.getHeader("User").getBytes()).toString();
    		}
    	}
    	
    	if(uuid==null) {
    		uuid = UUID.randomUUID().toString();
    	}
        return Controller.getInstance().addSong(url, (User) sc.getUserPrincipal(), uuid);
    }
    
    @PUT
    @Path("{id : .+}")
    @RolesAllowed("admin")
    @Consumes(MediaType.TEXT_PLAIN)
    public void sortsong(@PathParam("id") String sid, String prev) {
        try (
                Connection c = Controller.getInstance().getDB();
                PreparedStatement stmnt = c
                        .prepareStatement("select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC");
                PreparedStatement stmnt2 = c.prepareStatement("select * from PLAYLIST WHERE SONG_ID = ?");
                PreparedStatement stmnt3 = c.prepareStatement("UPDATE PLAYLIST SET SONG_SORT = ? WHERE SONG_ID = ?");
        ) {
            int id = Integer.parseInt(sid);
            int pr = -1;
            try {
                pr = Integer.parseInt(prev);
            } catch (NumberFormatException e) {
            }
            int low = Integer.MAX_VALUE;
            
            ResultSet rs = stmnt.executeQuery();
            if (rs.next()) {
                low = rs.getInt("SONG_SORT");
            }
            if (pr == -1) {
                pr = low - 1;
            } else {
                
                stmnt2.setInt(1, pr);
                ResultSet rs2 = stmnt2.executeQuery();
                rs2.next();
                pr = rs2.getInt("SONG_SORT");
            }
            
            stmnt3.setInt(1, pr + 1);
            stmnt3.setInt(2, id);
            stmnt3.addBatch();
            do {
                if (rs.getInt("SONG_ID") != id) {
                    if (rs.getInt("SONG_SORT") == pr + 1 && sc != null) {
                        low++;
                    }
                    if (rs.getInt("SONG_SORT") != low) {
                        stmnt3.setInt(1, low);
                        stmnt3.setInt(2, rs.getInt("Song_ID"));
                        stmnt3.addBatch();
                    }
                    low++;
                }
            } while (rs.next());
            stmnt3.executeBatch();
            if (sc != null) {
                Logger.getLogger(this.getClass()).info("Playlist sorted by: " + sc.getUserPrincipal());
            }
        } catch (NumberFormatException e) {
            throw new WebApplicationException(400);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException(e);
        }
    }
    
    private de.elite12.musikbot.shared.Song getSongbyID(int id) {
        try (
                Connection c = Controller.getInstance().getDB();
                PreparedStatement stmnt = c.prepareStatement("select * from PLAYLIST WHERE SONG_ID = ?");
        ) {
            stmnt.setInt(1, id);
            ResultSet rs = stmnt.executeQuery();
            if (rs.next()) {
                de.elite12.musikbot.shared.Song s = new de.elite12.musikbot.shared.Song(rs);
                User user = Controller.getInstance().getUserservice().getUserbyName(s.getAutor());
                s.setGravatarid(
                        user == null ? Util.md5Hex("null") : Util.md5Hex(user.getEmail().toLowerCase(Locale.GERMAN)));
                return s;
            } else {
                throw new javax.ws.rs.NotFoundException();
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQL ERROR", e);
        }
        return null;
    }
    
    private static String preparePlaceHolders(int length) {
        return String.join(",", Collections.nCopies(length, "?"));
    }
}
