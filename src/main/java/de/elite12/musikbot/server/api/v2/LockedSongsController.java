package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.CreateSongResponseDTO;
import de.elite12.musikbot.server.data.entity.LockedSong;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.songprovider.SongData;
import de.elite12.musikbot.server.exceptions.api.NotFoundException;
import de.elite12.musikbot.server.exceptions.songprovider.SongNotFound;
import de.elite12.musikbot.server.services.SongService;
import io.swagger.v3.oas.annotations.Operation;
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
public class LockedSongsController {

    private final Logger logger = LoggerFactory.getLogger(LockedSongsController.class);

    @Autowired
    private LockedSongRepository songs;

    @Autowired
    private SongService songService;

    @GetMapping
    @Operation(summary = "Gets the LockList", description = "Requires Admin Permissions.")
    public LockedSong[] getAction() {
        return StreamSupport.stream(songs.findAll().spliterator(), false).toArray(LockedSong[]::new);
    }

    @DeleteMapping(path = "{id}")
    @Operation(summary = "Deletes a Song from the Locklist", description = "Requires Admin Permissions.")
    public void deleteAction(@PathVariable Long id) {
        Optional<LockedSong> s = songs.findById(id);

        if(!s.isPresent()) throw new NotFoundException();

        songs.deleteById(id);

        logger.info(String.format("Locked Song removed by %s: %s", SecurityContextHolder.getContext().getAuthentication().getName(), s));
    }

    @PostMapping
    @Operation(summary = "Adds a Song to the Locklist", description = "Requires Admin Permissions.")
    public CreateSongResponseDTO postAction(@RequestBody String url) {
        try {
            SongData songData = this.songService.loadSong(url);

            LockedSong ls = new LockedSong();
            ls.updateFromSongData(songData);
            songs.save(ls);

            logger.info(String.format("Song locked by %s: %s", SecurityContextHolder.getContext().getAuthentication().getName(), ls));
            return new CreateSongResponseDTO(true, false, "Song hinzugefügt");
        } catch (IOException e) {
            logger.error("Error locking song",e);
            return new CreateSongResponseDTO(false, false, "Error locking song");
        } catch (SongNotFound e) {
            return new CreateSongResponseDTO(false, false, "Der eingegebene Song existiert nicht");
        }
    }
}
