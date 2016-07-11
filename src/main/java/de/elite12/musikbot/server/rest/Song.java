package de.elite12.musikbot.server.rest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import de.elite12.musikbot.server.Controller;
import de.elite12.musikbot.server.User;

@Path("/songs")
public class Song {

	@Context HttpServletRequest req;
	@Context SecurityContext sc;
	
	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Path("{ids : .+}")
	public de.elite12.musikbot.shared.Song[] getSong(@PathParam("ids") String sid) {
		String[] a = sid.split("/");
		de.elite12.musikbot.shared.Song[] r = new de.elite12.musikbot.shared.Song[a.length];
		try {
			for(int i = 0 ;i<a.length;i++) {
				int id = Integer.parseInt(a[i]);
				r[i] = getSongbyID(id);
			}
		}
		catch(NumberFormatException e) {
			throw new WebApplicationException(400);
		}
		return r;
	}
	
	@DELETE
	@RolesAllowed("admin")
	@Produces( MediaType.APPLICATION_JSON )
	@Path("{ids : .+}")
	public List<Integer> deleteSong(@PathParam("ids") String sid) {
		String[] a = sid.split("/");
		PreparedStatement stmnt = null;
		try {
			stmnt = Controller.getInstance().getDB().prepareStatement("DELETE FROM PLAYLIST WHERE SONG_ID = ? AND SONG_PLAYED = FALSE");
			for(String b :a) {
				int id = Integer.parseInt(b);
				stmnt.setInt(1, id);
				stmnt.addBatch();
			}
			Logger.getLogger(this.getClass()).info("Songs ("+ Arrays.toString(a)+ ") deleted by User: "+ sc.getUserPrincipal());
			return Arrays.asList(ArrayUtils.toObject(stmnt.executeBatch()));
		}
		catch(NumberFormatException e){
			throw new WebApplicationException(400);
		}
		catch (SQLException e) {
			Logger.getLogger(this.getClass()).error(
					"SQL Exception", e);
		} finally {
			try {
				stmnt.close();
			} catch (NullPointerException | SQLException e) {
				Logger.getLogger(this.getClass()).error(
						"Error closing Statement", e);
			}
		}
		throw new WebApplicationException(500);
	}
	
//	@DELETE
//	@RolesAllowed("admin")
//	@Produces( MediaType.APPLICATION_JSON )
//	@Path("lock/{ids : .+}")
//	public List<Integer> lockSong(@PathParam("ids") String sid) {
//		String[] a = sid.split("/");
//		PreparedStatement stmnt = null;
//		try {
//			String query = "SELECT * FROM PLAYLIST WHERE SONG_ID IN (%s) AND SONG_PLAYED = FALSE";
//			
//			stmnt = Controller.getInstance().getDB().prepareStatement(String.format(query, preparePlaceHolders(a.length)));
//			
//			for (int i = 0; i < a.length; i++) {
//				stmnt.setInt(i + 1, Integer.parseInt(a[i]));
//			}
//			
//			ResultSet resultSet = stmnt.executeQuery();
//			
//			Logger.getLogger(this.getClass()).info("Songs ("+ Arrays.toString(a)+ ") deleted and locked by User: "+ sc.getUserPrincipal());
//			return Arrays.asList(ArrayUtils.toObject(stmnt.executeBatch()));
//		}
//		catch(NumberFormatException e){
//			throw new WebApplicationException(400);
//		}
//		catch (SQLException e) {
//			Logger.getLogger(this.getClass()).error(
//					"SQL Exception", e);
//		} finally {
//			try {
//				stmnt.close();
//			} catch (NullPointerException | SQLException e) {
//				Logger.getLogger(this.getClass()).error(
//						"Error closing Statement", e);
//			}
//		}
//		throw new WebApplicationException(500);
//	}
	
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response createSong(String url) {	
		return Controller.getInstance().addSong(url, (User) sc.getUserPrincipal(), ((UUID) req.getSession().getAttribute("guest_id"))!=null?((UUID) req.getSession().getAttribute("guest_id")).toString():UUID.randomUUID().toString());
	}
	
	@PUT
	@Path("{id : .+}")
	@RolesAllowed("admin")
	@Consumes(MediaType.TEXT_PLAIN)
	public void sortsong(@PathParam("id") String sid, String prev) {
		try {
			int id = Integer.parseInt(sid);
			int pr = -1;
			try {
				pr = Integer.parseInt(prev);
			}
			catch (NumberFormatException e) {}
			int low = Integer.MAX_VALUE;
			PreparedStatement stmnt = Controller.getInstance().getDB().prepareStatement("select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC");
			ResultSet rs = stmnt.executeQuery();
			stmnt.close();
			if(rs.next()) {low = rs.getInt("SONG_SORT");}
			if(pr == -1) {pr = low-1;}
			else {
				stmnt = Controller.getInstance().getDB().prepareStatement("select * from PLAYLIST WHERE SONG_ID = ?");
				stmnt.setInt(1, pr);
				ResultSet rs2 = stmnt.executeQuery();
				rs2.next();
				pr=rs2.getInt("SONG_SORT");
				rs2.close();
				stmnt.close();
			}
			stmnt = Controller.getInstance().getDB().prepareStatement("UPDATE PLAYLIST SET SONG_SORT = ? WHERE SONG_ID = ?");
			stmnt.setInt(1, pr+1);
			stmnt.setInt(2, id);
			stmnt.addBatch();
			do {
				if(rs.getInt("SONG_ID") != id) {
					if(rs.getInt("SONG_SORT") == (pr+1) && sc != null) {low++;}
					if(rs.getInt("SONG_SORT") != low) {
						stmnt.setInt(1, low);
						stmnt.setInt(2, rs.getInt("Song_ID"));
						stmnt.addBatch();
					}
					low++;
				}
			} while(rs.next());
			stmnt.executeBatch();
			stmnt.close();
			rs.close();
			if(sc != null) {
				Logger.getLogger(this.getClass()).info("Playlist sorted by: "+ sc.getUserPrincipal());
			}
		}
		catch(NumberFormatException e) {
			throw new WebApplicationException(400);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		}
		
	}

	private de.elite12.musikbot.shared.Song getSongbyID(int id) {
		try {
			PreparedStatement stmnt = Controller.getInstance().getDB().prepareStatement("select * from PLAYLIST WHERE SONG_ID = ?");
			stmnt.setInt(1, id);
			ResultSet rs = stmnt.executeQuery();
			if(rs.next()) {
				return new de.elite12.musikbot.shared.Song(rs);
			}
			else {
				throw new javax.ws.rs.NotFoundException();
			}
		} catch (SQLException e) {
			Logger.getLogger(this.getClass()).error("SQL ERROR",e);
		}
		return null;
	}
	
	private static String preparePlaceHolders(int length) {
	    return String.join(",", Collections.nCopies(length, "?"));
	}
}
