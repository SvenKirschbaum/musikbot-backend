package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.StatsDTO;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.projection.TopSong;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.data.repository.UserRepository;
import io.swagger.annotations.ApiOperation;
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
public class Stats {

    @Autowired
    private EntityManager em;

    @Autowired
    private SongRepository songs;

    @Autowired
    private UserRepository users;

    @GetMapping
    @Cacheable(cacheNames = "stats", key = "'global'", sync = true)
    @ApiOperation(value = "Get Stats", notes = "Retrieve various Statistics")
    public StatsDTO getAction() {
        StatsDTO dto = new StatsDTO();

        dto.setMostPlayed(songs.findTopMostPlayed(PageRequest.of(0,10)).getContent());
        dto.setMostSkipped(songs.findTopMostSkipped(PageRequest.of(0,10)).getContent());
        dto.setTopUsers(songs.findTopUser(PageRequest.of(0,10)).getContent());

        User gapcloser = users.findByName("Automatisch");
        Long gaplcoserDuration = songs.getDurationByUserAuthor(gapcloser);

        dto.setGeneral(
            Arrays.asList(
                new StatsDTO.GeneralEntry("User", users.count()),
                new StatsDTO.GeneralEntry("Admins", users.countByAdmin(true)),
                new StatsDTO.GeneralEntry("Gäste", songs.countGuests()),
                new StatsDTO.GeneralEntry("Wünsche", songs.countByUserAuthorNot(gapcloser)),
                new StatsDTO.GeneralEntry("Davon übersprungen", songs.countByUserAuthorNotAndSkipped(gapcloser, true)),
                new StatsDTO.GeneralEntry("Generierte Songs", songs.countByUserAuthor(gapcloser)),
                new StatsDTO.GeneralEntry("Davon übersprungen", songs.countByUserAuthorAndSkipped(gapcloser, true)),
                new StatsDTO.GeneralEntry("Gesamte Wunsch-Dauer", String.format("%d Stunden", (songs.getCompleteDuration()-gaplcoserDuration)/3600)),
                new StatsDTO.GeneralEntry("Gesamte Generierte-Dauer", String.format("%d Stunden", gaplcoserDuration/3600))
            )
        );

        return dto;
    }

    @GetMapping(path = "/played")
    public List<TopSong> getMostPlayedAction() {
        return songs.findTopMostPlayed(PageRequest.of(0,100)).getContent();
    }

    @GetMapping(path = "/skipped")
    public List<TopSong> getMostSkippedAction() {
        return songs.findTopMostSkipped(PageRequest.of(0,100)).getContent();
    }

}
