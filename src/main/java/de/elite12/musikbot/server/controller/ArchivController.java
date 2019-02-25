package de.elite12.musikbot.server.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;

@Controller
@RequestMapping("/archiv")
public class ArchivController {
	
	@Autowired
	private SongRepository songs;
	
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ArchivController.class);

	@GetMapping
	public ModelAndView getAction(@RequestParam(value="p", defaultValue="1", required=false) int page) {
		Page<Song> archiv = songs.findByPlayedOrderBySortDesc(true, PageRequest.of(page, 30));
		ModelAndView r = new ModelAndView("archiv");
		r.addObject("page",archiv.getNumber());
		r.addObject("total",archiv.getTotalPages()-1);
		r.addObject("list",archiv);
		
		return r;
	}
	
}
