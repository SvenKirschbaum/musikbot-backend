package de.elite12.musikbot.server.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.model.User;
import de.elite12.musikbot.server.util.Util;
import de.elite12.musikbot.shared.Song;

@Path("/status")
public class Status {
    @Context
    private HttpServletRequest req;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public StatusUpdate getstatus() {
        StatusUpdate st = new StatusUpdate();

        st.status = Controller.getInstance().getState();
        st.songtitle = Controller.getInstance().getSongtitle();
        st.songlink = Controller.getInstance().getSonglink();
        st.playlist = new ArrayList<>(30);

        int dauer = 0;

        try (
                Connection c = Controller.getInstance().getDB();
                PreparedStatement stmnt = c
                        .prepareStatement("select * FROM PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC");
        ) {
            ResultSet rs = stmnt.executeQuery();
            while (rs.next()) {
                Song s = new Song(rs);
                dauer += s.getDauer();
                User user = Controller.getInstance().getUserservice().getUserbyName(s.getAutor());
                s.setGravatarid(
                        user == null ? Util.md5Hex("null") : Util.md5Hex(user.getEmail().toLowerCase(Locale.GERMAN)));
                st.playlist.add(s);
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("SQL Exception", e);
        }

        st.playlistdauer = dauer / 60;

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