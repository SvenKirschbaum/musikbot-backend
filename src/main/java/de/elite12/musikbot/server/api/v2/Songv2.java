package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.createSongResponse;
import de.elite12.musikbot.server.data.GuestSession;
import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.LockedSong;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.PushService;
import de.elite12.musikbot.server.services.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequestMapping("/v2/songs")
@RestController
public class Songv2 {

    @Autowired
	private SongService songservice;

    @Autowired
	private SongRepository songrepository;

    @Autowired
	private LockedSongRepository lockedsongrepository;

    @Autowired
	private GuestSession guestinfo;

    @Autowired
    private PushService pushService;
	
	private static final Logger logger = LoggerFactory.getLogger(Songv2.class);


    @RequestMapping(path="{ids}", method = RequestMethod.GET, produces = {"application/json"})
    public ResponseEntity<de.elite12.musikbot.server.data.entity.Song[]> getSong(@PathVariable String ids) {
        String[] a = ids.split(",");
        de.elite12.musikbot.server.data.entity.Song[] r = new de.elite12.musikbot.server.data.entity.Song[a.length];
        try {
            for (int i = 0; i < a.length; i++) {
                long id = Long.parseLong(a[i]);
                r[i] = songrepository.findById(id).orElseThrow();
            }
        } catch (NumberFormatException e) {
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(NoSuchElementException e) {
        	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(r, HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('admin')")
    @RequestMapping(path="{ids}", method = RequestMethod.DELETE, produces = {"application/json"})
    public ResponseEntity<Object> deleteSong(@PathVariable String ids, @RequestParam(value = "lock",required = false) Boolean lock) {
        String[] a = ids.split("/");
        lock = lock == null ? false : lock;
        try {
            for (String b : a) {
                long id = Long.parseLong(b);
                Optional<de.elite12.musikbot.server.data.entity.Song> song = songrepository.findById(id);
                if(!song.isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                
                if (lock) {
                    LockedSong ls = new LockedSong();
                    ls.setTitle(song.get().getTitle());
                    ls.setUrl(song.get().getLink());
                    
                    lockedsongrepository.save(ls);
                }
                
                songrepository.delete(song.get());
            }
            this.pushService.sendState();
            logger.info(String.format("Songs %s by %s: %s", lock ? "deleted and locked" : "deleted", ((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString(), Arrays.toString(a)));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NumberFormatException e) {
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @RequestMapping(path="", method = RequestMethod.POST, consumes = {"text/plain"}, produces = {"application/json"})
    public createSongResponse createSong(@RequestBody(required = false) String url) {
    	Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	User u = p instanceof UserPrincipal ? ((UserPrincipal) p).getUser() : null;

    	if(url == null) {
            return new createSongResponse(false, false, "Songlink kann nicht leer sein");
        }

        return songservice.addSong(url, u, guestinfo);
    }


    @PreAuthorize("hasRole('admin')")
    @RequestMapping(path="{ids}", method = RequestMethod.PUT)
    public ResponseEntity<Object> sortsong(@PathVariable("ids") String sid, @RequestBody(required=false) String prev) {
        try {
        	long id = Long.parseLong(sid);
        	long pr;
            try {
                pr = Long.parseLong(prev);
            } catch (NumberFormatException e) {
                pr = -1;
            }
            long low = Long.MAX_VALUE;
            
            Iterable<de.elite12.musikbot.server.data.entity.Song> songs = songrepository.findByPlayedOrderBySort(false);
            Iterator<de.elite12.musikbot.server.data.entity.Song> iterator = songs.iterator();
            de.elite12.musikbot.server.data.entity.Song cs;
            if (songs.iterator().hasNext()) {
            	cs = songs.iterator().next();
                low = cs.getSort();
            }
            if (pr == -1) {
                pr = low - 1;
            } else {
                pr = songrepository.findById(pr).orElseThrow().getSort();
            }
            
            de.elite12.musikbot.server.data.entity.Song s = songrepository.findById(id).orElseThrow();
            s.setSort(pr +1);
            songrepository.save(s);
            do {
            	cs = iterator.next();
                if (cs.getId() != id) {
                    if (cs.getSort() == pr + 1) {
                        low++;
                    }
                    if (cs.getSort() != low) {
                        cs.setSort(low);
                        songrepository.save(cs);
                    }
                    low++;
                }
            } while (iterator.hasNext());

            this.pushService.sendState();
            logger.info(String.format("Playlist sorted by %s", ((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString()));
        } catch (NumberFormatException | NoSuchElementException e) {
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
