package de.elite12.musikbot.server.api;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.GuestSession;
import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.LockedSong;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.SongService;

@RequestMapping("/api/songs")
@RestController
public class Song {
	
	@Autowired
	private SongService songservice;
	
	@Autowired
	private SongRepository songrepository;
	
	@Autowired
	private LockedSongRepository lockedsongrepository;
	
	@Autowired
	private GuestSession guestinfo;
	
	private static Logger logger = LoggerFactory.getLogger(Song.class);
    
    
    @RequestMapping(path="{ids}", method = RequestMethod.GET, produces = {"application/json"})
    public ResponseEntity<de.elite12.musikbot.server.data.entity.Song[]> getSong(@PathVariable String ids) {
        String[] a = ids.split(",");
        de.elite12.musikbot.server.data.entity.Song[] r = new de.elite12.musikbot.server.data.entity.Song[a.length];
        try {
            for (int i = 0; i < a.length; i++) {
                long id = Long.parseLong(a[i]);
                r[i] = songrepository.findById(id).get();
            }
        } catch (NumberFormatException e) {
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(NoSuchElementException e) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<de.elite12.musikbot.server.data.entity.Song[]>(r, HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('admin')")
    @RequestMapping(path="{ids}", method = RequestMethod.DELETE, produces = {"application/json"})
    public ResponseEntity<Object> deleteSong(@PathVariable String ids, @RequestParam("lock") Optional<Boolean> lock) {
        String[] a = ids.split("/");
        try {
            for (String b : a) {
                long id = Long.parseLong(b);
                Optional<de.elite12.musikbot.server.data.entity.Song> song = songrepository.findById(id);
                if(!song.isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                
                if (lock.isPresent() && lock.get()) {
                    LockedSong ls = new LockedSong();
                    ls.setTitle(song.get().getTitle());
                    ls.setUrl(song.get().getLink());
                    
                    lockedsongrepository.save(ls);
                }
                
                songrepository.delete(song.get());
            }
            logger.info("Songs (" + Arrays.toString(a) + ") deleted"
                    + (lock.orElseGet(() -> Boolean.FALSE) ? " and locked " : " ") + "by User: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NumberFormatException e) {
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @RequestMapping(path="", method = RequestMethod.POST, consumes = {"text/plain"})
    public ResponseEntity<String> createSong(@RequestBody String url) {
    	Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	logger.info(SecurityContextHolder.getContext().getAuthentication().toString());
    	User u = p instanceof UserPrincipal ? ((UserPrincipal) p).getUser() : null;
    	
        return songservice.addSong(url, u, guestinfo);
    }
    
    @PreAuthorize("hasRole('admin')")
    @RequestMapping(path="{ids}", method = RequestMethod.PUT)
    public ResponseEntity<Object> sortsong(@PathVariable("ids") String sid, @RequestBody(required=false) String prev) {
        try {
        	long id = Long.parseLong(sid);
        	long pr = -1;
            try {
                pr = Long.parseLong(prev);
            } catch (NumberFormatException e) {
            }
            long low = Long.MAX_VALUE;
            
            Iterable<de.elite12.musikbot.server.data.entity.Song> songs = songrepository.findByPlayedOrderBySort(false);
            Iterator<de.elite12.musikbot.server.data.entity.Song> iterator = songs.iterator();
            de.elite12.musikbot.server.data.entity.Song cs;
            if (songs.iterator().hasNext()) {
            	cs = songs.iterator().next();
                low = cs.getSort();
            }
            iterator = songs.iterator();
            if (pr == -1) {
                pr = low - 1;
            } else {
                pr = songrepository.findById(pr).get().getSort();
            }
            
            de.elite12.musikbot.server.data.entity.Song s = songrepository.findById(id).get();
            s.setSort(pr +1);
            songrepository.save(s);
            do {
            	cs = iterator.next();
                if (cs.getId() != id) {
                    if (cs.getSort() == pr + 1) {
                        low++;
                    }
                    if (cs.getSort() != low) {
                    	s = songrepository.findById(cs.getId()).get();
                        s.setSort(low);
                        songrepository.save(s);
                    }
                    low++;
                }
            } while (iterator.hasNext());
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
               logger.info("Playlist sorted by: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            }
        } catch (NumberFormatException e) {
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
