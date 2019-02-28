package de.elite12.musikbot.server.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UserMessage;
import de.elite12.musikbot.server.data.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.data.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.data.entity.LockedSong;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.services.MessageService;
import de.elite12.musikbot.server.services.SpotifyService;
import de.elite12.musikbot.server.services.YouTubeService;

@Controller
@RequestMapping("/songs")
@PreAuthorize("hasRole('admin')")
public class SongManagementController {

	@Autowired
	private LockedSongRepository lsongs;

	@Autowired
	private MessageService messages;
	
	@Autowired
	private YouTubeService youtube;
	
	@Autowired
	private SpotifyService spotify;

	private Logger logger = LoggerFactory.getLogger(SongManagementController.class);

	@GetMapping
	public String getAction(Model model) {
		messages.addMessage(
				"Hinweis: Admins sind in der Lage gesperrte Videos zur Playlist hinzuzufügen, bekommen jedoch eine Warnung angezeigt!",
				UserMessage.TYPE_NOTIFY);
		model.addAttribute("songs", lsongs.findAll());
		return "songmanagement";
	}

	@PostMapping
	public String doPost(@RequestParam(name = "action") String action, @RequestParam(name = "song") String song) {
		switch (action) {
			case "add": {
				try {
					UnifiedTrack ut = UnifiedTrack.fromURL(song,youtube,spotify);
					LockedSong ls = new LockedSong();
					ls.setTitle(ut.getTitle());
					ls.setUrl(ut.getId());
					lsongs.save(ls);

					
					logger.info("added Song to locklist: " + ut.getId() + "by User: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				} catch (TrackNotAvailableException e) {
					messages.addMessage("Der eingegebene Song existiert nicht", UserMessage.TYPE_ERROR);
				} catch (InvalidURLException e) {
					messages.addMessage("Die eingegebene URL ist ungültig", UserMessage.TYPE_ERROR);
				} catch (IOException e) {
					logger.error("Error locking song",e);
					messages.addMessage("Error locking song", UserMessage.TYPE_ERROR);
				}
				break;
			}
			case "delete": {
				String[] split = song.split(",");
				List<Long> ids =  new ArrayList<>();
				Arrays.stream(split).mapToLong((e) -> Long.parseLong(e)).forEach(ids::add);
				
				Iterable<LockedSong> ls = lsongs.findAllById(ids);
				
				lsongs.deleteAll(ls);
				
				logger.info("deleted Songs from locklist: "
						+ ids.toString() + " by User: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				
				break;
			}
			default: {
				break;
			}
		}
		return "redirect:/songs";
	}
}
