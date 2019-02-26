package de.elite12.musikbot.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.elite12.musikbot.server.services.GapcloserService;
import de.elite12.musikbot.server.services.GapcloserService.Mode;
import de.elite12.musikbot.shared.SongIDParser;
import de.elite12.musikbot.shared.SongIDParser.SpotifyPlaylistHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Controller
@RequestMapping("/gapcloser")
@PreAuthorize("hasRole('admin')")
public class GapcloserController {

    @Autowired
    private GapcloserService gapcloser;

    private static Logger logger = LoggerFactory.getLogger(GapcloserController.class);

    @GetMapping
    public String getAction(Model model) {
        GapcloserDTO dto = new GapcloserDTO(gapcloser.getMode(), gapcloser.getPlaylist());
        model.addAttribute("settings", dto);
        model.addAttribute("modes", Mode.values());
        return "gapcloser";
    }

    @PostMapping
    public String postAction(@ModelAttribute GapcloserDTO dto, Model model) {

        String pid = SongIDParser.getPID(dto.getPlaylist());
        String said = SongIDParser.getSAID(dto.getPlaylist());
        SpotifyPlaylistHelper spid = SongIDParser.getSPID(dto.getPlaylist());
        
        if (pid != null) {
            gapcloser.setPlaylist("https://www.youtube.com/playlist?list=" + pid);
        }
        if (said != null) {
            gapcloser.setPlaylist("https://open.spotify.com/album/" + said);
        }
        if (spid != null) {
            gapcloser.setPlaylist("https://open.spotify.com/user/" + spid.user + "/playlist/" + spid.pid);
        }

        gapcloser.setMode(dto.getMode());

        GapcloserDTO dto2 = new GapcloserDTO(gapcloser.getMode(), gapcloser.getPlaylist());
        model.addAttribute("settings", dto2);
        model.addAttribute("modes", Mode.values());
        
        logger.info("Gapcloser zu " + dto2.getMode().toString() + " geändert (Playlist: " + dto2.getPlaylist() + ")");
        
        return "gapcloser";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class GapcloserDTO {
        private Mode mode;
        private String playlist;
    }
}
