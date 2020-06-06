package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.api.dto.StatusUpdate;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.SongService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Comparator;

@RestController
@RequestMapping("/status")
public class Status {
    @Context
    private HttpServletRequest req;

    @Autowired
    private SongService songservice;

    @Autowired
    private SongRepository songrepository;

    @GetMapping()
    @ApiOperation(value = "Get the current Status")
    public StatusUpdate getstatus() {
        StatusUpdate st = new StatusUpdate();

        st.setStatus(songservice.getState().toString());
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

        //Resort songs because if a song has been added just now the sort field hasnt been persisted to the database yet, and is therefore not respected by the repository query
        list.sort(Comparator.comparingLong(Song::getSort));

        st.setPlaylist(list);

        st.setPlaylistdauer(dauer / 60);

        return st;
    }
}