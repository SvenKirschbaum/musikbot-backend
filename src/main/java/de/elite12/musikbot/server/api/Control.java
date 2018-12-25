package de.elite12.musikbot.server.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import de.elite12.musikbot.server.core.Controller;

@Path("/control")
@RolesAllowed("admin")
public class Control {
	
	@Context SecurityContext sc;
	
	@POST
	@Path("/start")
	public void start() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Controller.getInstance().getConnectionListener().getHandle().start();
			Logger.getLogger(this.getClass()).info("Botstart by User: "+ sc.getUserPrincipal());
		}
	}
	
	@POST
	@Path("/stop")
	public void stop() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Controller.getInstance().getConnectionListener().getHandle().stop();
			Logger.getLogger(this.getClass()).info("Botstop by User: "+ sc.getUserPrincipal());
		}
	}
	
	@POST
	@Path("/pause")
	public void pause() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Controller.getInstance().getConnectionListener().getHandle().pause();
			Logger.getLogger(this.getClass()).info("Botpause by User: "+ sc.getUserPrincipal());
		}
	}
	
	@POST
	@Path("/skip")
	public void skip() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Logger.getLogger(this.getClass()).info("Song skipped by User: "+ sc.getUserPrincipal()+ " Song: "+ Controller.getInstance().getSongtitle());
			Controller.getInstance().markskipped();
			Controller.getInstance().getConnectionListener().getHandle().stop();
			Controller.getInstance().getConnectionListener().getHandle().start();
		}
	}
	
	@POST
	@Path("/shuffle")
	public void shuffle() {
		try (
	        Connection c = Controller.getInstance().getDB();
	        PreparedStatement stmnt = c
	                .prepareStatement("select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC");
	        PreparedStatement update_stmnt = c.prepareStatement("UPDATE PLAYLIST SET SONG_SORT = ? WHERE SONG_ID = ?");
        ) {
			ArrayList<Pair<Integer, Integer>> ids = new ArrayList<>(30);
			ResultSet rs = stmnt.executeQuery();
			
			while(rs.next()) {
				ids.add(Pair.of(rs.getInt("SONG_ID"), rs.getInt("SONG_SORT")));
			}
			Collections.shuffle(ids);
			
			if(ids.size() == 0) return;
			
			for(int i = 0; i<ids.size(); i +=2 ) {
				Pair<Integer,Integer> a = ids.get(i);
				Pair<Integer,Integer> b = ids.get(i+1);
				
				update_stmnt.setInt(1, a.getRight());
				update_stmnt.setInt(2, b.getLeft());
				update_stmnt.addBatch();
				
				update_stmnt.setInt(1, b.getRight());
				update_stmnt.setInt(2, a.getLeft());
				update_stmnt.addBatch();
			}
			
			update_stmnt.executeBatch();
			
            if (sc != null) {
                Logger.getLogger(this.getClass()).info("Playlist shuffled by: " + sc.getUserPrincipal());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WebApplicationException(e);
        }
	}
}
