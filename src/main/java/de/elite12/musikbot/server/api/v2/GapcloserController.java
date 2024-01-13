package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.GapcloserConfigDTO;
import de.elite12.musikbot.server.api.dto.GapcloserUpdateConfigDTO;
import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.exception.BadRequestException;
import de.elite12.musikbot.server.services.GapcloserService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping(path = "/v2/gapcloser")
@MessageMapping("/gapcloser")
public class GapcloserController {

    private static final Logger logger = LoggerFactory.getLogger(GapcloserController.class);

    @Autowired
    private GapcloserService gapcloserService;

    @GetMapping
    @SubscribeMapping
    @Operation(summary = "Get Gapcloser Settings", description = "Requires Admin Permissions.")
    public GapcloserConfigDTO getAction() {
        return gapcloserService.getState();
    }

    @PutMapping
    @MessageMapping
    @SendTo("")
    @Operation(summary = "Updates Gapcloser Settings", description = "Requires Admin Permissions.")
    public GapcloserConfigDTO postAction(@RequestBody GapcloserUpdateConfigDTO req) {
        try {
            gapcloserService.setPlaylistFromUrl(req.getPlaylist());
            gapcloserService.setMode(req.getMode());

            GapcloserConfigDTO newstate = gapcloserService.getState();

            logger.info(String.format("Gapcloser changed by %s: %s", SecurityContextHolder.getContext().getAuthentication().getName(), newstate.toString()));

            return newstate;
        } catch (UnifiedTrack.InvalidURLException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}
