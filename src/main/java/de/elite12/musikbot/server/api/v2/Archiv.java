package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.ArchivDTO;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(path = "/v2/archiv")
public class Archiv {
    @Autowired
    private SongRepository songs;

    private static Logger logger = LoggerFactory.getLogger(Archiv.class);

    @GetMapping
    @RequestMapping(value = {"", "{page}"})
    public ArchivDTO getPage(@PathVariable(name = "page") Optional<Integer> opage) {
        int page = opage.orElse(1);

        Page<Song> archiv = songs.findByPlayedOrderBySortDesc(true, PageRequest.of(page-1, 25));

        return new ArchivDTO(archiv.getNumber()+1,archiv.getTotalPages(),archiv.get().toArray(Song[]::new));
    }

}
