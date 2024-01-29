package de.elite12.musikbot.backend.api.v2;

import de.elite12.musikbot.backend.api.dto.CreateSongResponseDTO;
import de.elite12.musikbot.backend.api.dto.PlaylistDTO;
import de.elite12.musikbot.backend.data.entity.User;
import de.elite12.musikbot.backend.data.songprovider.PlaylistData;
import de.elite12.musikbot.backend.exceptions.api.NotFoundException;
import de.elite12.musikbot.backend.exceptions.songprovider.PlaylistNotFound;
import de.elite12.musikbot.backend.services.JWTUserService;
import de.elite12.musikbot.backend.services.PlaylistService;
import de.elite12.musikbot.backend.services.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping(path = "/v2/playlist")
@PreAuthorize("hasRole('admin')")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private SongService songservice;

    @Autowired
    private JWTUserService jwtUserService;

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @GetMapping
    @Operation(summary = "Load playlist", description = "Gets the Song of a Youtube Playlist, a Spotify Playlist, a Spotify Artist or a Spotify Album. Requires Admin Permissions.")
    public PlaylistDTO getAction(@Parameter(name = "url", description = "The URL of the Playlist to be loaded") @RequestParam String url) {
        try {
            PlaylistData p = playlistService.loadPlaylist(url, true);

            logger.info(String.format("Playlist loaded by %s: %s", SecurityContextHolder.getContext().getAuthentication().getName(), p.toString()));

            PlaylistDTO playlistDTO = new PlaylistDTO();
            playlistDTO.setName(p.getTitle());
            playlistDTO.setLink(p.getCanonicalURL());
            playlistDTO.setTyp(p.getType());
            playlistDTO.setSongs(Arrays.stream(p.getEntries()).map(entry -> new PlaylistDTO.Entry(entry.getName(), entry.getLink())).toArray(PlaylistDTO.Entry[]::new));

            return playlistDTO;
        } catch (PlaylistNotFound e) {
            logger.debug("Playlist not found", e);
            throw new NotFoundException();
        } catch (IOException e) {
            logger.error("Error loading Playlist", e);
            throw new InternalServerErrorException();
        }
    }

    @PostMapping
    @Operation(summary = "Import Songs from a Playlist", description = "Adds multiple Songs to the Playlist. Requires Admin Permissions.")
    public CreateSongResponseDTO[] postAction(@RequestBody PlaylistDTO.Entry[] entries) {
        User u = jwtUserService.loadUserFromJWT((Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials());

        logger.info(String.format("Playlist imported by %s", u.toString()));

        return Arrays.stream(entries).map(entry -> songservice.addSong(entry.link, u, null)).toArray(CreateSongResponseDTO[]::new);
    }
}
