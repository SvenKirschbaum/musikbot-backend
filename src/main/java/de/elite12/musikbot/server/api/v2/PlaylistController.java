package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.PlaylistDTO;
import de.elite12.musikbot.server.api.dto.createSongResponse;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.exception.NotFoundException;
import de.elite12.musikbot.server.services.JWTUserService;
import de.elite12.musikbot.server.services.PlaylistImporterService;
import de.elite12.musikbot.server.services.SongService;
import de.elite12.musikbot.shared.util.SongIDParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping(path = "/v2/playlist")
@PreAuthorize("hasRole('admin')")
public class PlaylistController {

    @Autowired
    private PlaylistImporterService playlistImporterService;

    @Autowired
    private SongService songservice;

    @Autowired
    private JWTUserService jwtUserService;

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @GetMapping
    @Operation(summary = "Load playlist", description = "Gets the Song of a Youtube Playlist, a Spotify Playlist, a Spotify Artist or a Spotify Album. Requires Admin Permissions.")
    public PlaylistDTO getAction(@Parameter(name = "url", description = "The URL of the Playlist to be loaded") @RequestParam String url) {
        String pid = SongIDParser.getPID(url);
        String spid = SongIDParser.getSPID(url);
        String said = SongIDParser.getSAID(url);
        String sarid = SongIDParser.getSARID(url);

        if (pid == null && spid == null && said == null && sarid == null) {
            throw new NotFoundException();
        }

        PlaylistDTO p = null;
        if (pid != null) {
            p = playlistImporterService.getYoutubePlaylist(pid);
        }
        if (spid != null) {
            p = playlistImporterService.getSpotifyPlaylist(spid);
        }
        if (said != null) {
            p = playlistImporterService.getSpotifyAlbum(said);
        }
        if (sarid != null) {
            p = playlistImporterService.getSpotifyArtist(sarid);
        }

        logger.info(String.format("Playlist loaded by %s: %s", SecurityContextHolder.getContext().getAuthentication().getName(), p.toString()));

        return p;
    }

    @PostMapping
    @Operation(summary = "Import Songs from a Playlist", description = "Adds multiple Songs to the Playlist. Requires Admin Permissions.")
    public createSongResponse[] postAction(@RequestBody PlaylistDTO.Entry[] entries) {
        User u = jwtUserService.loadUserFromJWT((Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials());

        logger.info(String.format("Playlist imported by %s", u.toString()));

        return Arrays.stream(entries).map(entry -> songservice.addSong(entry.link, u, null)).toArray(createSongResponse[]::new);
    }
}
