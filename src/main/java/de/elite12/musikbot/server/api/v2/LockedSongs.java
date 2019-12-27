package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.createSongResponse;
import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.LockedSong;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.exception.NotFoundException;
import de.elite12.musikbot.server.services.SpotifyAPIService;
import de.elite12.musikbot.server.services.YouTubeService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping(path = "/v2/lockedsongs")
public class LockedSongs {

    private final Logger logger = LoggerFactory.getLogger(LockedSongs.class);

    @Autowired
    private LockedSongRepository songs;

    @Autowired
    private YouTubeService youtube;

    @Autowired
    private SpotifyAPIService spotifyAPIService;

    @GetMapping
    @ApiOperation(value = "Gets the LockList", notes = "Requires Admin Permissions.")
    public LockedSong[] getAction() {
        return StreamSupport.stream(songs.findAll().spliterator(), false).toArray(LockedSong[]::new);
    }

    @DeleteMapping(path = "{id}")
    @ApiOperation(value = "Deletes a Song from the Locklist", notes = "Requires Admin Permissions.")
    public void deleteAction(@PathVariable Long id) {
        Optional<LockedSong> s = songs.findById(id);

        if(!s.isPresent()) throw new NotFoundException();

        songs.deleteById(id);

        logger.info(String.format("Locked Song removed by %s: %s",((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString(),s.toString()));
    }

    @PostMapping
    @ApiOperation(value = "Adds a Song to the Locklist", notes = "Requires Admin Permissions.")
    public createSongResponse postAction(@RequestBody String url) {
        try {
            UnifiedTrack ut = UnifiedTrack.fromURL(url,youtube, spotifyAPIService);
            LockedSong ls = new LockedSong();
            ls.setTitle(ut.getTitle());
            ls.setUrl(ut.getLink());
            songs.save(ls);


            logger.info(String.format("Song locked by %s: %s",((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString(),ls.toString()));
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
