package de.elite12.musikbot.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.SongService;

@Controller
@RequestMapping("/")
public class IndexController {
	
	@Autowired
	SongService songService;
	
	@Autowired
	SongRepository songRepository;
	
	
	@GetMapping
	public String indexAction(Model m) {
		m.addAttribute("state",songService.getState());
		m.addAttribute("link",songService.getSonglink());
		m.addAttribute("title",songService.getSongtitle());
		
		Iterable<Song> playlist = songRepository.findByPlayedOrderBySort(false);
		m.addAttribute("playlist",playlist);
		
		int duration = 0;
		
		for(Song s:playlist) {
			duration += s.getDuration();
		}
		
		m.addAttribute("duration",duration);
		
		return "index";
	}
}
