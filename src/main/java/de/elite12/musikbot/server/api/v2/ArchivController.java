package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.ArchivDTO;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.exception.BadRequestException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/v2/archiv")
public class ArchivController {
    @Autowired
    private SongRepository songs;

    @GetMapping(value = {"", "{page}"})
    @ApiOperation(value = "Get already played Songs", notes = "Retrieves 25 Songs which have already been played. Use the page parameter to get older Songs.")
    public ArchivDTO getPage(@Min(1) @PathVariable(name = "page", required = false) Integer opage) {
            int page = opage == null ? 1 : opage;

            if (page < 1 || page >= 85899347) throw new BadRequestException("The page parameter is not in the required range");

            Page<Song> archiv = songs.findByPlayedOrderBySortDesc(true, PageRequest.of(page - 1, 25));

            return new ArchivDTO(archiv.getNumber() + 1, archiv.getTotalPages(), archiv.get().toArray(Song[]::new));
    }

}
