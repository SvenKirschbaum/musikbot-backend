package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.PlaylistDTO;
import de.elite12.musikbot.server.api.dto.createSongResponse;
import de.elite12.musikbot.server.data.GuestSession;
import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.exception.NotFoundException;
import de.elite12.musikbot.server.services.PlaylistImporterService;
import de.elite12.musikbot.server.services.SongService;
import de.elite12.musikbot.shared.SongIDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping(path = "/v2/playlist")
@PreAuthorize("hasRole('admin')")
public class Playlist {

    @Autowired
    private PlaylistImporterService playlistImporterService;

    @Autowired
    private SongService songservice;

    @Autowired
    private GuestSession guestinfo;

    private static final Logger logger = LoggerFactory.getLogger(Playlist.class);

    @GetMapping
    public PlaylistDTO getAction(@RequestParam String url) {
        String pid = SongIDParser.getPID(url);
        SongIDParser.SpotifyPlaylistHelper spid = SongIDParser.getSPID(url);
        String said = SongIDParser.getSAID(url);

        if (pid == null && spid == null && said == null) {
            throw new NotFoundException();
        }

        PlaylistDTO p = null;
        if (pid != null) {
            p = playlistImporterService.getyoutubePlaylist(pid);
        }
        if (spid != null) {
            p = playlistImporterService.getspotifyPlaylist(spid.user, spid.pid);
        }
        if (said != null) {
            p = playlistImporterService.getspotifyAlbum(said);
        }

        logger.info(String.format("Playlist loaded by %s: %s",((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString(),p.toString()));

        return p;
    }

    @PostMapping
    public createSongResponse[] postAction(@RequestBody PlaylistDTO.Entry[] entries) {
        User u = ((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        logger.info(String.format("Playlist imported by %s", u.toString()));

        return Arrays.stream(entries).map(entry -> songservice.addSong(entry.link,u,guestinfo)).toArray(createSongResponse[]::new);
    }
}
