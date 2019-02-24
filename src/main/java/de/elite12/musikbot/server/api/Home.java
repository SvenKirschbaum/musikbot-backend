package de.elite12.musikbot.server.api;

import java.util.ArrayList;

import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.repository.SongRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@RequestMapping("/api/home")
@RestController
public class Home {
	
	@Autowired SongRepository songrepository;
    
    @RequestMapping(path="", method = RequestMethod.GET, produces = {"application/json"})
    public SearchResult[] autocomplete(@RequestParam("term") String term) {
    	
    		Iterable<Tuple> res = songrepository.findSearchResult(term);
    		ArrayList<SearchResult> al = new ArrayList<>();
    		res.forEach((Tuple t) -> {
    			al.add(new SearchResult((String) t.get(0), (String) t.get(1)));
    		});
    		
    		return al.toArray(new SearchResult[0]);
    }
    
    @Getter
	@Setter
	@AllArgsConstructor
	public static class SearchResult {
		private String value;
		private String label;
	}
}