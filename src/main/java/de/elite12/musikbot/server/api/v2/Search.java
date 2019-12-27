package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.SearchResult;
import de.elite12.musikbot.server.data.repository.SongRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Tuple;
import java.util.ArrayList;

@RequestMapping("/v2/search")
@RestController
public class Search {

    @Autowired
    private SongRepository songrepository;

    @GetMapping
    @ApiOperation(value = "Search the Song Database")
    public SearchResult[] autocomplete(@ApiParam(name = "term", value = "The Searchterm") @RequestParam(required = false) String term) {
        if (term == null) return new SearchResult[0];
        Iterable<Tuple> res = songrepository.findSearchResult(term);
        ArrayList<SearchResult> al = new ArrayList<>();
        res.forEach(t -> al.add(new SearchResult(t.get(1, String.class), t.get(0, String.class))));

        return al.toArray(new SearchResult[0]);
    }

    @MessageMapping("/search")
    @SendToUser("/queue/search")
    public SearchResult[] autocomplete(Message<String> message) {
        return this.autocomplete(message.getPayload());
    }
}