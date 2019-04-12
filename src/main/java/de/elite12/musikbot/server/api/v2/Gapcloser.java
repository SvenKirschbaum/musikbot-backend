package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.services.GapcloserService;
import de.elite12.musikbot.shared.SongIDParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v2/gapcloser")
public class Gapcloser {

    @Autowired
    private GapcloserService gapcloserService;

    @GetMapping
    public GapcloserDTO getAction() {
        return new GapcloserDTO(gapcloserService.getPlaylist(), gapcloserService.getMode());
    }

    @PostMapping
    public GapcloserDTO postAction(@RequestBody GapcloserDTO req) {
        String pid = SongIDParser.getPID(req.getPlaylist());
        String said = SongIDParser.getSAID(req.getPlaylist());
        SongIDParser.SpotifyPlaylistHelper spid = SongIDParser.getSPID(req.getPlaylist());

        if (pid != null) {
            gapcloserService.setPlaylist("https://www.youtube.com/playlist?list=" + pid);
        }
        if (said != null) {
            gapcloserService.setPlaylist("https://open.spotify.com/album/" + said);
        }
        if (spid != null) {
            gapcloserService.setPlaylist("https://open.spotify.com/user/" + spid.user + "/playlist/" + spid.pid);
        }

        gapcloserService.setMode(req.getMode());

        return getAction();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GapcloserDTO {
        private String playlist;
        private GapcloserService.Mode mode;
    }
}
