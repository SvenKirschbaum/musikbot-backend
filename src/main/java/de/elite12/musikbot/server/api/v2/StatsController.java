package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.StatsDTO;
import de.elite12.musikbot.server.data.projection.TopSong;
import de.elite12.musikbot.server.data.repository.GuestRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.data.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(path = "/v2/stats")
public class StatsController {

    @Autowired
    private EntityManager em;

    @Autowired
    private SongRepository songs;

    @Autowired
    private UserRepository users;

    @Autowired
    private GuestRepository guests;

    @GetMapping
    @Cacheable(cacheNames = "stats", key = "'global'", sync = true)
    @Operation(summary = "Get Stats", description = "Retrieve various Statistics")
    public StatsDTO getAction() {
        StatsDTO dto = new StatsDTO();

        dto.setMostPlayed(songs.findTopMostPlayed(PageRequest.of(0, 10)).getContent());
        dto.setMostSkipped(songs.findTopMostSkipped(PageRequest.of(0, 10)).getContent());
        dto.setTopUsers(songs.findTopUser(PageRequest.of(0, 10)).getContent());

        Long gaplcoserDuration = songs.getSystemSongsDuration();

        dto.setGeneral(
                Arrays.asList(
                        new StatsDTO.GeneralEntry("User", users.count()),
                        new StatsDTO.GeneralEntry("Admins", users.countByAdmin(true)),
                        new StatsDTO.GeneralEntry("Gäste", guests.count()),
                        new StatsDTO.GeneralEntry("Wünsche", songs.countNonSystem()),
                        new StatsDTO.GeneralEntry("Davon übersprungen", songs.countNonSystemSkipped()),
                        new StatsDTO.GeneralEntry("Generierte Songs", songs.countSystem()),
                        new StatsDTO.GeneralEntry("Davon übersprungen", songs.countSystemSkipped()),
                        new StatsDTO.GeneralEntry("Gesamte Wunsch-Dauer", String.format("%d Stunden", (songs.getCompleteDuration() - gaplcoserDuration) / 3600)),
                        new StatsDTO.GeneralEntry("Gesamte Generierte-Dauer", String.format("%d Stunden", gaplcoserDuration / 3600))
                )
        );

        return dto;
    }

    @GetMapping(path = "/played")
    @Cacheable(cacheNames = "stats", key = "'played'", sync = true)
    public List<TopSong> getMostPlayedAction() {
        return songs.findTopMostPlayed(PageRequest.of(0,100)).getContent();
    }

    @GetMapping(path = "/skipped")
    @Cacheable(cacheNames = "stats", key = "'skipped'", sync = true)
    public List<TopSong> getMostSkippedAction() {
        return songs.findTopMostSkipped(PageRequest.of(0,100)).getContent();
    }

}
