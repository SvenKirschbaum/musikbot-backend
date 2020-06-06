package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.StatsDTO;
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

        dto.setMostplayed(songs.findTopMostPlayed(PageRequest.of(0,10)).getContent());
        dto.setMostskipped(songs.findTopMostSkipped(PageRequest.of(0,10)).getContent());
        dto.setTopuser(songs.findTopUser(PageRequest.of(0,10)).getContent());

        dto.setGeneral(
            Arrays.asList(
                new StatsDTO.GeneralEntry("User", users.count()),
                new StatsDTO.GeneralEntry("Admins", users.countByAdmin(true)),
                new StatsDTO.GeneralEntry("Gäste", songs.countGuests()),
                new StatsDTO.GeneralEntry("Wünsche", songs.count()),
                new StatsDTO.GeneralEntry("Skippes", songs.countBySkipped(true)),
                new StatsDTO.GeneralEntry("Gesamte Dauer", String.format("%d Stunden", songs.getCompleteDuration()/3600))
            )
        );

        return dto;
    }

}
