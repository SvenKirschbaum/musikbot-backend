package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.api.dto.VolumeDTO;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;


@RequestMapping("/control")
@PreAuthorize("hasRole('admin')")
@RestController
public class ControlController {

	@Autowired
	private ClientService client;

	@Autowired
	private SongRepository songrepository;

	private static final Logger logger = LoggerFactory.getLogger(ControlController.class);

    @RequestMapping(path = "/start", method = RequestMethod.POST)
    @Operation(summary = "Start Playback", description = "Instructs the connected Client to start Playback. Requires Admin Permissions.")
	public void start() {
        client.start();
        logger.info(String.format("Botstart by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @RequestMapping(path = "/stop", method = RequestMethod.POST)
    @Operation(summary = "Start Playback", description = "Instructs the connected Client to start Playback. Requires Admin Permissions.")
	public void stop() {
        client.stop();
        logger.info(String.format("Botstop by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @RequestMapping(path = "/pause", method = RequestMethod.POST)
    @Operation(summary = "Pause Playback", description = "Instructs the connected Client to pause Playback. Requires Admin Permissions.")
	public void pause() {
        client.pause();
        logger.info(String.format("Botpause by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @RequestMapping(path = "/skip", method = RequestMethod.POST)
    @Operation(summary = "Skip Song", description = "Instructs the connected Client to stop the current Song and continue with the next Song in queue. Requires Admin Permissions.")
	public void skip() {
		client.stop();
		client.start();
		logger.info(String.format("Song skipped by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
	}

    @RequestMapping(path = "/volume", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set Volume", description = "Sets the playback volume. Requires Admin Permissions.")
	public void volume(
            @RequestBody
            @Parameter(name = "volume", description = "The Volume to play at in Percent")
			VolumeDTO volume) {
        this.client.sendVolume(volume.getVolume());
        logger.info(String.format("Volume set to %d%% by User %s", volume.getVolume(), SecurityContextHolder.getContext().getAuthentication().getName()));
    }

	@RequestMapping(path = "/shuffle", method = RequestMethod.POST)
	@Operation(summary = "Shuffle Playlist", description = "Shuffles the Playlist. Requires Admin Permissions.")
	@Transactional
	public void shuffle() {
		List<Song> songs = songrepository.findByPlayedOrderBySort(false);

		Collections.shuffle(songs);

		for (int i = 0; i < songs.size(); i++) {
			Song s = songs.get(i);

			s.setSort(i + 1.0);

			songrepository.save(s);
		}

		logger.info(String.format("Playlist shuffled by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
	}
}
