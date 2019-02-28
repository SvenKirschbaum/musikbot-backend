package de.elite12.musikbot.server.controller;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.elite12.musikbot.server.data.repository.SongRepository;

@Controller
@RequestMapping("/statistik")
public class StatistikController {
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private SongRepository songs;
	
	@GetMapping
	public String getAction(Model model) {
		model.addAttribute("mostplayed", songs.findTopMostPlayed());
		model.addAttribute("mostskipped", songs.findTopMostSkipped());
		model.addAttribute("topusers", songs.findTopUser());
		
		Query q = em.createNativeQuery("select count(*) from user UNION ALL select count(*) from user WHERE admin = TRUE UNION ALL SELECT Count(*) FROM (SELECT guest_author FROM song WHERE CHAR_LENGTH(guest_author) = 36 GROUP BY guest_author) AS T UNION ALL select count(*) from song WHERE (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) UNION ALL select count(*) from song WHERE skipped = TRUE UNION ALL select sum(duration) from song WHERE skipped = FALSE;");
		@SuppressWarnings("rawtypes")
		List r = q.getResultList();
		model.addAttribute("allgemein", r);
		return "stats";
	}
}
