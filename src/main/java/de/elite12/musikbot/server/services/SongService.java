package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.api.dto.createSongResponse;
import de.elite12.musikbot.server.config.ServiceProperties;
import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.data.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.data.entity.Guest;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.GapcloserService.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class SongService {
    
    private static final Logger logger = LoggerFactory.getLogger(SongService.class);

    @Autowired
    private SongRepository songrepository;

    @Autowired
    private LockedSongRepository lockedSongRepository;

    @Autowired
    private ServiceProperties config;

    @Autowired
    private GapcloserService gapcloser;

    @Autowired
    private YouTubeService youtube;

    @Autowired
    private SpotifyService spotifyService;

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
            UnifiedTrack.fromSong(next, youtube, spotifyService);
        }
        catch(IOException e) {
        	logger.error("Error Loading Track", e);
            return null;
        }
        catch(TrackNotAvailableException e) {
        	logger.warn("Song seems to got deleted, skipping", e);
            next.setSkipped(true);
            songrepository.save(next);
            return this.getnextSong();
        } catch (InvalidURLException e) {
        	logger.error("Impossible Error",e);
        	return this.getnextSong();
		}
        
        return next;
    }

    public createSongResponse addSong(String url, User user, Guest guest) {
        try {
            logger.debug("Trying to Add Song " + url);
            UnifiedTrack ut = UnifiedTrack.fromURL(url, youtube, spotifyService);
            String notice = null;

            if (lockedSongRepository.countByUrl(ut.getLink()) > 0) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song is locked, but User is Admin, creating Notice");
                    notice = "Hinweis: Dieser Song wurde gesperrt!";
                } else {
                    logger.debug("Song is locked, denying");
                    return new createSongResponse(false,false,"Dieser Song wurde leider gesperrt!");
                }
            }

            if(songrepository.countByPlayed(false) >= 24) {
                logger.debug("Adding Song aborted, Playlist is full");
                return new createSongResponse(false,false,"Die Playlist ist leider voll!");
            }

            if(songrepository.countByLinkAndPlayed(ut.getLink(),false) > 0) {
                logger.debug("Adding Song aborted, Song allready in Playlist");
                return new createSongResponse(false,false,"Dieser Song befindet sich bereits in der Playlist!");
            }

            Long count = user != null ? songrepository.countByUserAuthorAndPlayed(user, false) : songrepository.countByGuestAuthorAndPlayed(guest, false);
            if(count > 2 && (user == null || !user.isAdmin())) {
                logger.debug("Adding Song aborted, User reached maximum");
                return new createSongResponse(false,false,"Du hast bereits die maximale Anzahl an Songs eingestellt!");
            }

            if(ut.getDuration() > 600 && (user == null || !user.isAdmin())) {
                logger.debug("Adding Song aborted, Song to long");
                return new createSongResponse(false,false,"Dieses Video ist leider zu lang!");
            }
            if (!config.getYoutube().getCategories().contains(ut.getCategoryId())) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song is not in allowed Categorys, but User is Admin, creating Notice");
                    notice = "Hinweis: Dieser Song befindet sich nicht in einer der erlaubten Kategorien!";
                } else {
                    logger.debug("Song is not in allowed Categorys, denying");
                    return new createSongResponse(false,false,"Dieses Song gehört nicht zu einer der erlaubten Kategorien!");
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
                s.setGuestAuthor(guest);
            }


            s = songrepository.save(s);

            logger.info(String.format("Song added by %s: %s", user != null ? ("User " + user.getName()) : ("Guest " + guest.getIdentifier()), s.toString()));


            client.notifynewSong();

            if(notice != null) {
                return new createSongResponse(true,true,notice);
            }
            else {
                return new createSongResponse(true,false,"Song erfolgreich hinzugefügt");
            }
        } catch (IOException e) {
            logger.error("Error adding Song" ,e);
            return new createSongResponse(false,false, "Unbekannter Fehler");
        } catch (TrackNotAvailableException e) {
            logger.debug("Track not found",e);
            return new createSongResponse(false,false, "Song nicht verfügbar: " + e.getMessage());
        } catch (InvalidURLException e) {
            logger.debug("Invalid URL",e);
            return new createSongResponse(false,false, "URL ungültig");
        }
    }
}
