package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.api.dto.CreateSongResponseDTO;
import de.elite12.musikbot.server.data.entity.Guest;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.data.songprovider.SongData;
import de.elite12.musikbot.server.exceptions.songprovider.SongNotFound;
import de.elite12.musikbot.server.interfaces.SongProvider;
import de.elite12.musikbot.server.services.GapcloserService.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Service
public class SongService {

    private static final Logger logger = LoggerFactory.getLogger(SongService.class);
    private final SongProvider[] provider;

    private final SongRepository songrepository;

    private final LockedSongRepository lockedSongRepository;

    private final GapcloserService gapcloser;

    private final ClientService client;

    public SongService(
            ListableBeanFactory listableBeanFactory,
            SongRepository songrepository,
            LockedSongRepository lockedSongRepository,
            GapcloserService gapcloser,
            ClientService client
    ) {
        this.provider = listableBeanFactory.getBeansOfType(SongProvider.class).values().toArray(SongProvider[]::new);
        this.songrepository = songrepository;
        this.lockedSongRepository = lockedSongRepository;
        this.gapcloser = gapcloser;
        this.client = client;
    }

    public Song getnextSong() {
    	Song next = songrepository.getNextSong();
    	if(next == null) {
    		logger.debug("No further Song found");
            if (gapcloser.getMode() != Mode.OFF) {
                SongData songData = gapcloser.getnextSong();

                if (songData == null) {
                    return null;
                }

                Song filler = new Song();
                filler.setPlayed(true);
                filler.setInsertedAt(new Date());
                filler.setPlayedAt(new Date());
                filler.updateFromSongData(songData);

                return songrepository.save(filler);
            } else {
                return null;
            }
    	}

        next.setPlayed(true);
        next.setPlayedAt(new Date());

        Optional<SongProvider> optionalSongProvider = Arrays.stream(this.provider).filter(p -> p.supportsSongUrl(next.getLink())).findFirst();
        if (optionalSongProvider.isEmpty()) {
            logger.warn("No provider found for song {}, skipping", next);
            next.setSkipped(true);
            songrepository.save(next);
            return this.getnextSong();
        }

        SongProvider provider = optionalSongProvider.get();

        try {
            SongData song = provider.getSong(next.getLink());
            next.updateFromSongData(song);
            return songrepository.save(next);
        } catch (IOException | SongNotFound e) {
            logger.warn("Song seems to got deleted, skipping", e);
            next.setSkipped(true);
            songrepository.save(next);
            return this.getnextSong();
        }
    }

    public SongData loadSong(String url) throws IOException, SongNotFound {
        Optional<SongProvider> optionalSongProvider = Arrays.stream(this.provider).filter(p -> p.supportsSongUrl(url)).findFirst();
        if (optionalSongProvider.isEmpty()) {
            logger.debug("No provider found for song {}", url);
            throw new SongNotFound("Die verwendete URL ist unbekannt");
        }

        SongProvider provider = optionalSongProvider.get();
        return provider.getSong(url);
    }

    public CreateSongResponseDTO addSong(String url, User user, Guest guest) {
        Optional<SongProvider> optionalSongProvider = Arrays.stream(this.provider).filter(p -> p.supportsSongUrl(url)).findFirst();

        if (optionalSongProvider.isEmpty()) {
            logger.debug("No Provider found for URL {}", url);
            return new CreateSongResponseDTO(false, false, "Die verwendete URL ist unbekannt");
        }

        SongProvider provider = optionalSongProvider.get();

        try {
            SongData song = provider.getSong(url);
            List<String> notices = new ArrayList<>();

            // Check if Song is locked
            if (lockedSongRepository.countByUrl(song.getCanonicalURL()) > 0) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song is locked, but User is Admin, creating Notice");
                    notices.add("Hinweis: Dieser Song wurde gesperrt!");
                } else {
                    logger.debug("Song is locked, denying");
                    return new CreateSongResponseDTO(false, false, "Dieser Song wurde leider gesperrt!");
                }
            }

            // Check if Playlist is full
            if(songrepository.countByPlayed(false) >= 24) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Playlist is full, but User is Admin, creating Notice");
                    notices.add("Hinweis: Die Playlist ist voll");
                } else {
                    logger.debug("Adding Song aborted, Playlist is full");
                    return new CreateSongResponseDTO(false, false, "Die Playlist ist leider voll!");
                }
            }

            // Check if Song is already in Playlist
            if (songrepository.countByLinkAndPlayed(song.getCanonicalURL(), false) > 0) {
                logger.debug("Adding Song aborted, Song already in Playlist");
                return new CreateSongResponseDTO(false, false, "Dieser Song befindet sich bereits in der Playlist!");
            }

            // Check if User reached maximum
            Long count = user != null ? songrepository.countByUserAuthorAndPlayed(user, false) : songrepository.countByGuestAuthorAndPlayed(guest, false);
            if (count > 4 && (user == null || !user.isAdmin())) {
                logger.debug("Adding Song aborted, User reached maximum");
                return new CreateSongResponseDTO(false, false, "Du hast bereits die maximale Anzahl an Songs eingestellt!");
            }

            // Check if Song is to long
            if (song.getDuration().compareTo(Duration.ofSeconds(600)) > 0 && (user == null || !user.isAdmin())) {
                logger.debug("Adding Song aborted, Song to long");
                return new CreateSongResponseDTO(false, false, "Dieser Song ist leider zu lang!");
            }


            // Get non admin restrictions of song
            if (!song.getGetNonAdminRestrictions().isEmpty()) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song has non admin restrictions, but User is Admin, creating Notice");
                    notices.addAll(song.getGetNonAdminRestrictions());
                } else {
                    logger.debug("Song has non admin restrictions, denying");
                    return new CreateSongResponseDTO(false, false, String.join("\n", song.getGetNonAdminRestrictions()));
                }
            }

            Song entity = new Song();
            entity.updateFromSongData(song);
            entity.setInsertedAt(new Date());
            entity.setPlayed(false);

            if(user != null) {
                entity.setUserAuthor(user);
            }
            else {
                entity.setGuestAuthor(guest);
            }

            songrepository.save(entity);

            logger.info(String.format("Song added by %s: %s", user != null ? ("User " + user.getName()) : ("Guest " + guest.getIdentifier()), entity));

            client.notifynewSong();

            if (!notices.isEmpty()) {
                return new CreateSongResponseDTO(true, true, String.join("\n", notices));
            }
            else {
                return new CreateSongResponseDTO(true, false, "Song erfolgreich hinzugefügt");
            }
        } catch (SongNotFound e) {
            logger.debug("Song not found", e);
            return new CreateSongResponseDTO(false, false, "Song nicht gefunden");
        } catch (IOException e) {
            logger.debug("Error loading Song", e);
            return new CreateSongResponseDTO(false, false, "Beim laden des Songs ist ein Fehler aufgetreten");
        }
    }
}
