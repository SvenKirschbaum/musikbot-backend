package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.api.dto.VolumeDTO;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.ClientService;
import de.elite12.musikbot.server.services.PushService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@RequestMapping("/control")
@PreAuthorize("hasRole('admin')")
@RestController
public class ControlController {

	@Autowired
	private ClientService client;

	@Autowired
	private SongRepository songrepository;
	
	private static final Logger logger = LoggerFactory.getLogger(ControlController.class);

	@Autowired
	private PushService pushService;

	@RequestMapping(path="/start", method = RequestMethod.POST)
	@ApiOperation(value = "Start Playback", notes = "Instructs the connected Client to start Playback. Requires Admin Permissions.")
	public void start() {
        client.start();
        logger.info(String.format("Botstart by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
    }
	
	@RequestMapping(path="/stop", method = RequestMethod.POST)
	@ApiOperation(value = "Start Playback", notes = "Instructs the connected Client to start Playback. Requires Admin Permissions.")
	public void stop() {
        client.stop();
        logger.info(String.format("Botstop by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
    }
	
	@RequestMapping(path="/pause", method = RequestMethod.POST)
	@ApiOperation(value = "Pause Playback", notes = "Instructs the connected Client to pause Playback. Requires Admin Permissions.")
	public void pause() {
        client.pause();
        logger.info(String.format("Botpause by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
    }
	
	@RequestMapping(path="/skip", method = RequestMethod.POST)
	@ApiOperation(value = "Skip Song", notes = "Instructs the connected Client to stop the current Song and continue with the next Song in queue. Requires Admin Permissions.")
	public void skip() {
		client.stop();
		client.start();
		logger.info(String.format("Song skipped by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
	}

	@RequestMapping(path="/volume", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Set Volume", notes = "Sets the playback volume. Requires Admin Permissions.")
	public void volume(
			@RequestBody
			@ApiParam(name = "volume", value = "The Volume to play at in Percent")
			VolumeDTO volume) {
        this.client.sendVolume(volume.getVolume());
        logger.info(String.format("Volume set to %d%% by User %s", volume.getVolume(), SecurityContextHolder.getContext().getAuthentication().getName()));
    }
	
	@RequestMapping(path="/shuffle", method = RequestMethod.POST)
	@ApiOperation(value = "Shuffle Playlist", notes = "Shuffles the Playlist. Requires Admin Permissions.")
	public void shuffle() {
		ArrayList<Pair<Long, Long>> ids = new ArrayList<>(30);
		Map<Long, Song> list = new HashMap<>(30);
		Iterable<Song> tmp = songrepository.findByPlayedOrderBySort(false);
		
		for(Song s:tmp) {
			ids.add(Pair.of(s.getId(), s.getSort()));
			list.put(s.getId(), s);
		}
		Collections.shuffle(ids);
		
		if(ids.size() == 0) return;
		
		for(int i = 0; i<(ids.size()-1); i +=2 ) {
			Pair<Long, Long> a = ids.get(i);
			Pair<Long, Long> b = ids.get(i+1);
			
			Song t = list.get(b.getFirst());
			t.setSort(a.getSecond());
			songrepository.save(t);
			
			t = list.get(a.getFirst());
			t.setSort(b.getSecond());
			songrepository.save(t);
		}

        this.pushService.sendState();
        logger.info(String.format("Playlist shuffled by %s", SecurityContextHolder.getContext().getAuthentication().getName()));
	}
}
