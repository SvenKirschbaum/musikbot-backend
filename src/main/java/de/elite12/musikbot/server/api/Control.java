package de.elite12.musikbot.server.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.ClientService;
import de.elite12.musikbot.server.services.SongService;
import de.elite12.musikbot.server.data.entity.Song;


@RequestMapping("/control")
@PreAuthorize("hasRole('admin')")
@RestController
public class Control {
	
	@Autowired
	private ClientService client;
	
	@Autowired
	private SongService songservice;
	
	@Autowired
	private SongRepository songrepository;
	
	private static Logger logger = LoggerFactory.getLogger(Control.class);
	
	@RequestMapping(path="/start", method = RequestMethod.POST)
	public void start() {
		client.start();
		logger.info("Botstart by User: "+ SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}
	
	@RequestMapping(path="/stop", method = RequestMethod.POST)
	public void stop() {
		client.stop();
		logger.info("Botstop by User: "+ SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}
	
	@RequestMapping(path="/pause", method = RequestMethod.POST)
	public void pause() {
		client.pause();
		logger.info("Botpause by User: "+ SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}
	
	@RequestMapping(path="/skip", method = RequestMethod.POST)
	public void skip() {
		logger.info("Song skipped by User: "+ SecurityContextHolder.getContext().getAuthentication().getPrincipal()+ " Song: "+ songservice.getSongtitle());
		songservice.markskipped();
		client.stop();
		client.start();
	}
	
	@RequestMapping(path="/shuffle", method = RequestMethod.POST)
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
		
        logger.info("Playlist shuffled by: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}
}
