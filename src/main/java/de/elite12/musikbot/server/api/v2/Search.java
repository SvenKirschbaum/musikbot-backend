package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.data.repository.SongRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Optional;

@RequestMapping("/api/v2/search")
@RestController
public class Search {
	
	@Autowired SongRepository songrepository;
    
    @RequestMapping(path="", method = RequestMethod.POST, produces = {"application/json"}, consumes = {"text/plain"})
    public SearchResult[] autocomplete(@RequestBody Optional<String> term) {

    		if(!term.isPresent()) return new SearchResult[0];
    		Iterable<Tuple> res = songrepository.findSearchResult(term.get());
    		ArrayList<SearchResult> al = new ArrayList<>();
    		res.forEach((Tuple t) -> {
    			al.add(new SearchResult((String) t.get(1), (String) t.get(0)));
    		});

    		return al.toArray(new SearchResult[0]);
    }
    
    @Getter
	@Setter
	@AllArgsConstructor
	@ToString
	public static class SearchResult {
		private String value;
		private String label;
	}
}