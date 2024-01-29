package de.elite12.musikbot.backend.api.v2;

import de.elite12.musikbot.backend.data.projection.SearchResult;
import de.elite12.musikbot.backend.data.repository.SongRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v2/search")
@MessageMapping("/search")
public class SearchController {

    @Autowired
    private SongRepository songrepository;

    @GetMapping
    @MessageMapping
    @SendToUser(broadcast = false)
    @Operation(summary = "Search the Song Database")
    public List<SearchResult> autocomplete(@Payload @Parameter(name = "term", description = "The Searchterm") @RequestParam(required = false) String term) {
        if (term == null) return Collections.emptyList();
        Page<SearchResult> res = songrepository.findSearchResult(term, PageRequest.of(0, 10));
        return res.getContent();
    }
}
