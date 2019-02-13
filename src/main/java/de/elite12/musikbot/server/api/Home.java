package de.elite12.musikbot.server.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import de.elite12.musikbot.server.core.Controller;

@Path("/home")
public class Home {
    @Context
    private HttpServletRequest req;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public SearchEntry[] autocomplete(@QueryParam("term") String term) {
    	try (
			Connection c = Controller.getInstance().getDB();
	        PreparedStatement stmnt = c.prepareStatement("select SONG_NAME,SONG_LINK from PLAYLIST WHERE AUTOR != 'Automatisch' AND (SONG_LINK LIKE ? ESCAPE '$' OR SONG_NAME LIKE ? ESCAPE '$') GROUP BY SONG_LINK ORDER BY count(*) DESC LIMIT 10");
		) {
    		term = term
			    .replace("$", "$$")
			    .replace("%", "$%")
			    .replace("_", "$_")
			    .replace("[", "$[");
    		stmnt.setString(1, "%"+term+"%");
    		stmnt.setString(2, "%"+term+"%");
    		ResultSet rs = stmnt.executeQuery();
    		ArrayList<SearchEntry> ret = new ArrayList<>();
    		
    		while(rs.next()) {
    			ret.add(new SearchEntry(rs.getString(2), rs.getString(1)));
    		}
    		
    		return ret.toArray(new SearchEntry[0]);
    	}
    	catch(SQLException e) {
    		throw new WebApplicationException(e);
    	}
    }
    
    private class SearchEntry {
    	String value;
    	String label;
    	
    	public SearchEntry(String value, String label) {
    		this.value = value;
    		this.label = label;
    	}
    }
}