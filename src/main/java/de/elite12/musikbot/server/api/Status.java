package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.api.dto.StatusUpdate;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.ArrayList;

@RestController
@RequestMapping("/status")
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

        st.setStatus(songservice.getState());
        st.setSongtitle(songservice.getSongtitle());
        st.setSonglink(songservice.getSonglink());
        st.setVolume(songservice.getVolume());

        ArrayList<Song> list = new ArrayList<>(30);

        int dauer = 0;
        
        Iterable<Song> songs = songrepository.findByPlayedOrderBySort(false);

       
        for (Song s: songs) {
            dauer += s.getDuration();
            list.add(s);
        }

        st.setPlaylist(list);

        st.setPlaylistdauer(dauer / 60);

        return st;
    }
}