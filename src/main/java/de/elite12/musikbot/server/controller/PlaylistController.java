package de.elite12.musikbot.server.controller;

import java.util.Optional;

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

import de.elite12.musikbot.server.data.UserMessage;
import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.MessageService;
import de.elite12.musikbot.server.services.PlaylistImporterService;
import de.elite12.musikbot.server.services.PlaylistImporterService.Playlist;
import de.elite12.musikbot.server.services.SongService;
import de.elite12.musikbot.shared.SongIDParser;
import de.elite12.musikbot.shared.SongIDParser.SpotifyPlaylistHelper;

@Controller
@RequestMapping("/import/")
@PreAuthorize("hasRole('admin')")
public class PlaylistController {

	@Autowired
	private MessageService messages;
	
	@Autowired
	private PlaylistImporterService pimport;
	
	@Autowired
	private SongService songservice;
	
	private static Logger logger = LoggerFactory.getLogger(PlaylistController.class);

	@GetMapping
	public String getAction() {
		messages.addMessage("Hinweis: Es werden nur die ersten 400 Videos einer Playlist angezeigt!",
				UserMessage.TYPE_NOTIFY);
		return "import";
	}

	@PostMapping
	public String postAction(@RequestParam(name="playlist") String playlist, @RequestParam(name="pimport", required=false) Optional<Integer[]> ids, Model model) {
		String pid = SongIDParser.getPID(playlist);
		SpotifyPlaylistHelper spid = SongIDParser.getSPID(playlist);
		String said = SongIDParser.getSAID(playlist);
		
		if (pid == null && spid == null && said == null) {
			messages.addMessage("Playlist Link ungültig", UserMessage.TYPE_ERROR);
			return "import";
		}
		
		Playlist p = null;
		if (pid != null) {
			p = pimport.getyoutubePlaylist(pid);
		}
		if (spid != null) {
			p = pimport.getspotifyPlaylist(spid.user, spid.pid);
		}
		if (said != null) {
			p = pimport.getspotifyAlbum(said);
		}
		
		
		
		if(!ids.isPresent()) {
			model.addAttribute("link", playlist);
			model.addAttribute("list", p);
			return "import-list";
		}
		
		Integer[] i = ids.get();
		UserPrincipal up = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User u = up.getUser();

		for (Integer l : i) {
			try {
				songservice.addSong(p.entrys[l].link, u, null);
			} catch (Exception e) {
				logger.error("pimport hat falsches Format: ", e);
				messages.addMessage("Fehler", UserMessage.TYPE_ERROR);
				return "import";
			}
		}
		logger.info("Playlist Importiert: " + playlist + " by User: " + u);
		return "redirect:/";
	}
}
