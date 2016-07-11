package de.elite12.musikbot.server.rest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.Controller;
import de.elite12.musikbot.server.User;
import de.elite12.musikbot.shared.Song;

@Path("/status")
public class Status {
	@Context
	private HttpServletRequest req;
	  
	  
	@GET
	@Produces( MediaType.APPLICATION_JSON )
	public StatusUpdate getstatus() {
		StatusUpdate st = new StatusUpdate();
		
		st.status = Controller.getInstance().getState();
		st.songtitle = Controller.getInstance().getSongtitle();
		st.songlink = Controller.getInstance().getSonglink();
		st.playlist = new ArrayList<>(30);
		
		int dauer = 0;

		PreparedStatement stmnt = null;
		ResultSet rs = null;
		try {
			stmnt = Controller
					.getInstance()
					.getDB()
					.prepareStatement(
							"select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC");
			rs = stmnt.executeQuery();
			while (rs.next()) {
				Song s = new Song(rs);
				dauer += s.getDauer();
				if (!(req.getSession().getAttribute("user") != null && ((User) req
						.getSession().getAttribute("user")).isAdmin())) {
					try {
						UUID.fromString(s.getAutor());
						s.setAutor("Gast");
					} catch (IllegalArgumentException e) {
					}
				}
				st.playlist.add(s);
			}
		} catch (SQLException e) {
			Logger.getLogger(this.getClass()).error("SQL Exception", e);
		} finally {
			try {
				rs.close();
			} catch (NullPointerException | SQLException e) {
				Logger.getLogger(this.getClass()).error(
						"Exception closing ResultSet", e);
			}
			try {
				stmnt.close();
			} catch (NullPointerException | SQLException e) {
				Logger.getLogger(this.getClass()).error(
						"Exception closing Statement", e);
			}
		}
		
		st.playlistdauer = dauer/60;
		
		return st;
	}
}

@XmlRootElement
class StatusUpdate {
	@XmlElement
	String status;
	@XmlElement
	String songtitle;
	@XmlElement
	String songlink;
	@XmlElement
	int playlistdauer;
	@XmlElement
	ArrayList<Song> playlist;
}