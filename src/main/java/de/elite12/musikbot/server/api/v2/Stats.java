package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.StatsDTO;
import de.elite12.musikbot.server.data.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/api/v2/stats")
public class Stats {

    @Autowired
    private EntityManager em;

    @Autowired
    private SongRepository songs;

    @GetMapping
    public StatsDTO getAction() {
        StatsDTO dto = new StatsDTO();

        dto.setMostplayed(
                StreamSupport.stream(songs.findTopMostPlayed().spliterator(), false)
                        .map(
                                tuple ->
                                        new StatsDTO.TopSong(
                                                tuple.get(0, String.class),
                                                tuple.get(1, String.class),
                                                tuple.get(2, BigInteger.class).intValue()
                                        )
                        )
                        .collect(Collectors.toList())
        );

        dto.setMostskipped(
                StreamSupport.stream(songs.findTopMostSkipped().spliterator(), false)
                        .map(
                                tuple ->
                                        new StatsDTO.TopSong(
                                                tuple.get(0, String.class),
                                                tuple.get(1, String.class),
                                                tuple.get(2, BigInteger.class).intValue()
                                        )
                        )
                        .collect(Collectors.toList())
        );

        dto.setTopuser(
                StreamSupport.stream(songs.findTopUser().spliterator(), false)
                        .map(
                                tuple ->
                                        new StatsDTO.TopUser(
                                                tuple.get(0, String.class),
                                                tuple.get(1, BigInteger.class).intValue()
                                        )
                        )
                        .collect(Collectors.toList())
        );

        Query q = em.createNativeQuery("select count(*) from user UNION ALL select count(*) from user WHERE admin = TRUE UNION ALL SELECT Count(*) FROM (SELECT guest_author FROM song WHERE CHAR_LENGTH(guest_author) = 36 GROUP BY guest_author) AS T UNION ALL select count(*) from song WHERE (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) UNION ALL select count(*) from song WHERE skipped = TRUE UNION ALL select sum(duration) from song WHERE skipped = FALSE;");

        Object[] list = q.getResultList().toArray();

        dto.setGeneral(
            Arrays.asList(
                new StatsDTO.GeneralEntry("User", ((BigDecimal)list[0]).intValue()),
                new StatsDTO.GeneralEntry("Admins", ((BigDecimal)list[1]).intValue()),
                new StatsDTO.GeneralEntry("Gäste", ((BigDecimal)list[2]).intValue()),
                new StatsDTO.GeneralEntry("Wünsche", ((BigDecimal)list[3]).intValue()),
                new StatsDTO.GeneralEntry("Skippes", ((BigDecimal)list[4]).intValue()),
                new StatsDTO.GeneralEntry("Gesamte Dauer", ((BigDecimal)list[5]).intValue())
            )
        );

        return dto;
    }

}
