package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UserMessage;
import de.elite12.musikbot.server.api.dto.createSongResponse;
import de.elite12.musikbot.server.data.entity.LockedSong;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.services.SpotifyService;
import de.elite12.musikbot.server.services.YouTubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping(path = "/v2/lockedsongs")
public class LockedSongs {

    private Logger logger = LoggerFactory.getLogger(LockedSongs.class);

    @Autowired
    private LockedSongRepository songs;

    @Autowired
    private YouTubeService youtube;

    @Autowired
    private SpotifyService spotify;

    @GetMapping
    public LockedSong[] getAction() {
        return StreamSupport.stream(songs.findAll().spliterator(), false).collect(Collectors.toList()).toArray(new LockedSong[0]);
    }

    @DeleteMapping(path = "{id}")
    public void deleteAction(@PathVariable Long id) {
        songs.deleteById(id);
    }

    @PostMapping
    public createSongResponse postAction(@RequestBody String url) {
        try {
            UnifiedTrack ut = UnifiedTrack.fromURL(url,youtube,spotify);
            LockedSong ls = new LockedSong();
            ls.setTitle(ut.getTitle());
            ls.setUrl(ut.getLink());
            songs.save(ls);


            logger.info("added Song to locklist: " + ut.getId() + "by User: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return new createSongResponse(true,false,"Song hinzugefügt");
        } catch (UnifiedTrack.TrackNotAvailableException e) {
            return new createSongResponse(false,false,"Der eingegebene Song existiert nicht");
        } catch (UnifiedTrack.InvalidURLException e) {
            return new createSongResponse(false,false,"Die eingegebene URL ist ungültig");
        } catch (IOException e) {
            logger.error("Error locking song",e);
            return new createSongResponse(false,false,"Error locking song");
        }
    }
}
