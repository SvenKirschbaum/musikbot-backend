package de.elite12.musikbot.server.api;

import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.SongService;
import de.elite12.musikbot.server.util.Util;
import lombok.Getter;

@RestController
@RequestMapping("/api/status")
public class Status {
    @Context
    private HttpServletRequest req;
    
    @Autowired
    private SongService songservice;
    
    @Autowired
    private SongRepository songrepository;
    
    @RequestMapping(path = "", produces = {"application/json"})
    public StatusUpdate getstatus() {
        StatusUpdate st = new StatusUpdate();

        st.status = songservice.getState();
        st.songtitle = songservice.getSongtitle();
        st.songlink = songservice.getSonglink();
        st.playlist = new ArrayList<>(30);

        int dauer = 0;
        
        Iterable<Song> songs = songrepository.findByPlayedOrderBySort(false);

       
        for (Song s: songs) {
            dauer += s.getDuration();
            User user = s.getUserAuthor();
            s.setGravatarid(
                    user == null ? Util.md5Hex("null") : Util.md5Hex(user.getEmail().toLowerCase(Locale.GERMAN)));
            s.setAuthor(user == null ? s.getGuestAuthor() : s.getUserAuthor().getName());
            st.playlist.add(s);
        }

        st.playlistdauer = dauer / 60;

        return st;
    }
}

@Getter
class StatusUpdate {
    String status;
    String songtitle;
    String songlink;
    int playlistdauer;
    ArrayList<Song> playlist;
}