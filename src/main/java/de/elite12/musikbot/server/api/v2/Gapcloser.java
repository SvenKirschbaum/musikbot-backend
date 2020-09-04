package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.GapcloserDTO;
import de.elite12.musikbot.server.services.GapcloserService;
import de.elite12.musikbot.shared.util.SongIDParser;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping(path = "/v2/gapcloser")
public class Gapcloser {

    private static final Logger logger = LoggerFactory.getLogger(Gapcloser.class);

    @Autowired
    private GapcloserService gapcloserService;

    @GetMapping
    @ApiOperation(value = "Get Gapcloser Settings", notes = "Requires Admin Permissions.")
    public GapcloserDTO getAction() {
        return new GapcloserDTO(gapcloserService.getPlaylist(), gapcloserService.getMode());
    }

    @PutMapping
    @ApiOperation(value = "Updates Gapcloser Settings", notes = "Requires Admin Permissions.")
    public GapcloserDTO postAction(@RequestBody GapcloserDTO req) {
        String pid = SongIDParser.getPID(req.getPlaylist());
        String said = SongIDParser.getSAID(req.getPlaylist());
        String spid = SongIDParser.getSPID(req.getPlaylist());

        if (pid != null) {
            gapcloserService.setPlaylist("https://www.youtube.com/playlist?list=" + pid);
        }
        if (said != null) {
            gapcloserService.setPlaylist("https://open.spotify.com/album/" + said);
        }
        if (spid != null) {
            gapcloserService.setPlaylist("https://open.spotify.com/playlist/" + spid);
        }

        gapcloserService.setMode(req.getMode());

        GapcloserDTO newstate = getAction();

        logger.info(String.format("Gapcloser changed by %s: %s", SecurityContextHolder.getContext().getAuthentication().getName(), newstate.toString()));

        return newstate;
    }

}
