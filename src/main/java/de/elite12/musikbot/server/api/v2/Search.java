package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.SearchResult;
import de.elite12.musikbot.server.data.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Optional;

@RequestMapping("/v2/search")
@RestController
public class Search {

    @Autowired
    SongRepository songrepository;

    @RequestMapping(path = "", method = RequestMethod.POST, produces = {"application/json"}, consumes = {"text/plain"})
    public SearchResult[] autocomplete(@RequestBody Optional<String> term) {

        if (!term.isPresent()) return new SearchResult[0];
        Iterable<Tuple> res = songrepository.findSearchResult(term.get());
        ArrayList<SearchResult> al = new ArrayList<>();
        res.forEach(t -> al.add(new SearchResult(t.get(1, String.class), t.get(0, String.class))));

        return al.toArray(new SearchResult[0]);
    }

}