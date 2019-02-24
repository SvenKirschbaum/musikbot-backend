package de.elite12.musikbot.server.services;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.elite12.musikbot.server.core.MusikbotServiceProperties;
import de.elite12.musikbot.server.data.GuestSession;
import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.data.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.GapcloserService.Mode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class SongService {
	
    private String songtitle = "Kein Song";
    private String state = "Keine Verbindung zum BOT";
    private String songlink;
    
    private static final Logger logger = LoggerFactory.getLogger(SongService.class);
    
    @Autowired
    private SongRepository songrepository;
    
    @Autowired
    private LockedSongRepository lockedrepository;
    
    @Autowired
    private MusikbotServiceProperties config;
    
    @Autowired
    private GapcloserService gapcloser;
    
    @Autowired 
    private YouTubeService youtube;
    
    @Autowired 
    private SpotifyService spotify;
    
    @Autowired
    private ClientService client;

    public Song getnextSong() {
    	Song next = songrepository.getNextSong();
    	if(next == null) {
    		logger.debug("No further Song found");
            if (gapcloser.getMode() != Mode.OFF) {
                return gapcloser.getnextSong();
            } else {
                return null;
            }
    	}
        next.setPlayed(true);
        next.setPlayedAt(new Date());
        next = songrepository.save(next);
        
        try {
        	UnifiedTrack.fromSong(next, youtube,spotify);
        }
        catch(IOException e) {
        	logger.error("Error Loading Track", e);
            return null;
        }
        catch(TrackNotAvailableException e) {
        	logger.warn("Song seems to got deleted, skipping", e);
            return this.getnextSong();
        } catch (InvalidURLException e) {
        	logger.error("Impossible Error",e);
        	return this.getnextSong();
		}
        
        return next;
    }    

    public void markskipped() {
        logger.debug("Marking last Song as skipped");

        Song last = songrepository.getLastSong();
        
        if(last == null) {
        	return;
        }
        
        last.setSkipped(true);
        songrepository.save(last);
    }

    public ResponseEntity<String> addSong(String url, User user, GuestSession gi) {
    	try {
    		logger.debug("Trying to Add Song "+url);
			UnifiedTrack ut = UnifiedTrack.fromURL(url, youtube, spotify);
			String notice = null;
            
           if (lockedrepository.countByUrl(ut.getLink()) > 0) {
				if (user != null && user.isAdmin()) {
					logger.debug("Song is locked, but User is Admin, creating Notice");
					notice = "Hinweis: Dieser Song wurde gesperrt!";
				} else {
					logger.debug("Song is locked, denying");
					return new ResponseEntity<String>("Dieser Song wurde leider gesperrt!",HttpStatus.FORBIDDEN);
				}
            }
            
            if(songrepository.countByPlayed(false) >= 24) {
            	logger.debug("Adding Song aborted, Playlist is full");
            	return new ResponseEntity<String>("Die Playlist ist leider voll!",HttpStatus.FORBIDDEN);
            }
            
            if(songrepository.countByLinkAndPlayed(ut.getLink(),false) > 0) {
            	logger.debug("Adding Song aborted, Song allready in Playlist");
            	return new ResponseEntity<String>("Dieser Song befindet sich bereits in der Playlist!",HttpStatus.FORBIDDEN);
            }
            
            Long count = user != null ? songrepository.countByUserAuthor(user) : songrepository.countByGuestAuthor(gi.getId());
            if(count > 2 && (user == null || !user.isAdmin())) {
            	logger.debug("Adding Song aborted, User reached maximum");
            	return new ResponseEntity<String>("Du hast bereits die maximale Anzahl an Songs eingestellt!",HttpStatus.FORBIDDEN);
            }
            
            if(ut.getDuration() > 600 && (user == null || !user.isAdmin())) {
	        	logger.debug("Adding Song aborted, Song to long");
	        	return new ResponseEntity<String>("Dieses Video ist leider zu lang!",HttpStatus.CONFLICT);
            }
            if (!config.getYoutube().getCategories().contains(ut.getCategoryId())) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song is not in allowed Categorys, but User is Admin, creating Notice");
                    notice = "Hinweis: Dieser Song befindet sich nicht in einer der erlaubten Kategorien!";
                } else {
                    logger.debug("Song is not in allowed Categorys, denying");
                    return new ResponseEntity<String>("Dieses Song gehört nicht zu einer der erlaubten Kategorien!",HttpStatus.FORBIDDEN);
                }
            }
            
            Song s = new Song();
            s.setPlayed(false);
            s.setLink(ut.getLink());
            s.setTitle(ut.getTitle());
            s.setDuration(ut.getDuration());
            s.setInsertedAt(new Date());
            
            if(user != null) {
            	s.setUserAuthor(user);
            }
            else {
                s.setGuestAuthor(gi.getId());
            }
            
            
            s = songrepository.save(s);
            
            logger.info("Succesfully added Song (ID: " + s.getId() + ") to Playlist: " + ut.getId()
                    + " by " + (user != null ? ("User: " + user.getName()) : ("Guest: " + gi.getId())));
            
            

            client.notifynewSong();
            
            return new ResponseEntity<String>(notice != null ? notice : "Song erfolgreich hinzugefügt",HttpStatus.CREATED);
		} catch (IOException e) {
			logger.error("Error adding Song" ,e);
			return new ResponseEntity<String>("Unbekannter Fehler",HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (TrackNotAvailableException e) {
			logger.debug("Track not found",e);
			return new ResponseEntity<String>("Song nicht verfügbar: " + e.getMessage(),HttpStatus.NOT_FOUND);
		} catch (InvalidURLException e) {
			logger.debug("Invalid URL",e);
			return new ResponseEntity<String>("URL ungültig",HttpStatus.BAD_REQUEST);
		}
    }
}
