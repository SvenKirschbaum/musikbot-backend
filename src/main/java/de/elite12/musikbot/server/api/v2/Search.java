package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.data.projection.SearchResult;
import de.elite12.musikbot.server.data.repository.SongRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RequestMapping("/v2/search")
@RestController
public class Search {

    @Autowired
    private SongRepository songrepository;

    @GetMapping
    @ApiOperation(value = "Search the Song Database")
    public List<SearchResult> autocomplete(@ApiParam(name = "term", value = "The Searchterm") @RequestParam(required = false) String term) {
        if (term == null) return Collections.emptyList();
        Page<SearchResult> res = songrepository.findSearchResult(term, PageRequest.of(0,10));
        return res.getContent();
    }

    @MessageMapping("/search")
    @SendToUser("/queue/search")
    public List<SearchResult> autocomplete(Message<String> message) {
        return this.autocomplete(message.getPayload());
    }
}