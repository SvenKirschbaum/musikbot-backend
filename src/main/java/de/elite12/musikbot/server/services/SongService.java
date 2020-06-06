package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.api.dto.createSongResponse;
import de.elite12.musikbot.server.config.MusikbotServiceProperties;
import de.elite12.musikbot.server.data.GuestSession;
import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.data.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.data.entity.Setting;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SettingRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.GapcloserService.Mode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class SongService {
	
	@Getter
	@Setter
    private String songtitle = null;
	@Getter
	@Setter
    private State state = State.NOT_CONNECTED;
	@Getter
	@Setter
    private String songlink;
    @Getter
    private short volume = 38;

    @Getter
    @Setter
    private ProgressInfo progressInfo = null;
    
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
    private SpotifyAPIService spotifyAPIService;

    @Autowired
    private ClientService client;

    @Autowired
    private PushService pushService;

    @Autowired
    private SettingRepository settings;

    @PostConstruct
    public void postConstruct() {
        Optional<Setting> volume = settings.findById("volume");

        if(volume.isPresent()) {
            this.volume = Short.parseShort(volume.get().getValue());
        }
        else {
            this.setVolume((short) 38);
        }
    }

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
        	UnifiedTrack.fromSong(next, youtube, spotifyAPIService);
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

    public void markskipped() {
        logger.debug("Marking last Song as skipped");

        Song last = songrepository.getLastSong();
        
        if(last == null) {
        	return;
        }
        
        last.setSkipped(true);
        songrepository.save(last);
    }

    public createSongResponse addSong(String url, User user, GuestSession gi) {
        try {
            logger.debug("Trying to Add Song "+url);
            UnifiedTrack ut = UnifiedTrack.fromURL(url, youtube, spotifyAPIService);
            String notice = null;

            if (lockedrepository.countByUrl(ut.getLink()) > 0) {
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

            Long count = user != null ? songrepository.countByUserAuthorAndPlayed(user,false) : songrepository.countByGuestAuthorAndPlayed(gi.getId(),false);
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
                s.setGuestAuthor(gi.getId());
            }


            s = songrepository.save(s);

            logger.info(String.format("Song added by %s: %s", user != null ? ("User " + user.getName()) : ("Guest " + gi.getId()), s.toString()));


            client.notifynewSong();
            pushService.sendState();

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

    public void setVolume(short volume) {
        this.volume = volume;

        Setting volumesetting = new Setting("volume", Short.toString(volume));

        settings.save(volumesetting);
    }

    public enum State {
        NOT_CONNECTED,
        CONNECTED,
        PAUSED,
        STOPPED,
        PLAYING,
        WAITING_FOR_SONGS;


        @Override
        public String toString() {
            switch (this) {
                case NOT_CONNECTED:
                    return "Keine Verbindung zum BOT";
                case CONNECTED:
                    return "Verbunden";
                case PAUSED:
                    return "Pausiert";
                case STOPPED:
                    return "Gestoppt";
                case PLAYING:
                    return "Playing";
                case WAITING_FOR_SONGS:
                    return "Warte auf neue Lieder";
                default:
                    return "";
            }
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ProgressInfo {
        Instant start;
        Duration duration;
        Duration prepausedDuration;
        boolean paused;

        public void pause() {
            prepausedDuration = prepausedDuration.plus(Duration.between(start, Instant.now()));
            this.paused = true;
        }

        public void unpause() {
            this.start = Instant.now();
            this.paused = false;
        }
    }
}
