package de.elite12.musikbot.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.elite12.musikbot.server.api.dto.GapcloserConfigDTO;
import de.elite12.musikbot.server.data.entity.Setting;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SettingRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.data.songprovider.PlaylistData;
import de.elite12.musikbot.server.data.songprovider.SongData;
import de.elite12.musikbot.server.exceptions.songprovider.PlaylistNotFound;
import de.elite12.musikbot.server.exceptions.songprovider.SongNotFound;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class GapcloserService {

    public enum Mode {
        OFF,
        RANDOM100,
        RANDOM,
        PLAYLIST
    }

    @Getter
    private Mode mode = Mode.OFF;

    @Getter
    @Nullable
    private PlaylistData playlistData = null;

    @Getter
    private List<GapcloserConfigDTO.HistoryEntry> playlistHistory = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Permutationhelper permutation;

    @Autowired
    private SettingRepository settings;

    @Autowired
    private SongRepository songs;

    @Autowired
    @Lazy
    private SongService songService;

    @Autowired
    private LockedSongRepository lockedSongRepository;

    @Autowired
    private StateService stateService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private PlaylistService playlistService;

    private static final Logger logger = LoggerFactory.getLogger(GapcloserService.class);

    @PostConstruct
    public void postConstruct() {
        Optional<Setting> mode = settings.findById("gapcloser");
        Optional<Setting> playlist = settings.findById("playlist");
        Optional<Setting> playlistHistory = settings.findById("playlistHistory");

        mode.ifPresent(setting -> this.mode = Mode.valueOf(setting.getValue()));
        playlist.ifPresent(setting -> {
            try {
                this.loadPlaylistData(setting.getValue());
            } catch (IOException | PlaylistNotFound e) {
                if (this.mode == Mode.PLAYLIST) {
                    this.mode = Mode.OFF;
                }
            }
        });
        playlistHistory.ifPresent(setting -> {
            try {
                this.playlistHistory = objectMapper.readValue(setting.getValue(), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                logger.error("Error reading Playlist History", e);
            }
        });

        if (mode.isEmpty() || playlist.isEmpty() || playlistHistory.isEmpty()) {
            this.save();
        }
    }

    @SneakyThrows
    private void save() {
    	Setting modesetting = new Setting("gapcloser", this.mode.toString());
        Setting playlistsetting = new Setting("playlist", this.playlistData != null ? this.playlistData.getCanonicalURL() : "");
        Setting historySetting = new Setting("playlistHistory", this.objectMapper.writeValueAsString(this.playlistHistory));

        settings.saveAll(Arrays.asList(modesetting, playlistsetting, historySetting));

        template.convertAndSend("/topic/gapcloser", this.getState());

    	logger.debug("Einstellung " + modesetting + " wurde gespeichert");
    	logger.debug("Einstellung " + playlistsetting + " wurde gespeichert");
        logger.debug("Einstellung " + historySetting + " wurde gespeichert");
    }

    private void loadPlaylistData(String url) throws IOException, PlaylistNotFound {
        PlaylistData oldData = this.playlistData;
        this.playlistData = this.playlistService.loadPlaylist(url);
        this.permutation = new Permutationhelper(this.playlistData.getLength());
        this.updateHistory(oldData);
    }

    public SongData getnextSong() {
        for (int i = 0; i < 5; i++) {
            try {
                SongData song = switch (this.getMode()) {
                    case OFF -> null;
                    case RANDOM -> {
                        Optional<Song> s = songs.getRandomSong();
                        if (s.isPresent()) {
                            yield this.songService.loadSong(s.get().getLink());
                        } else {
                            yield null;
                        }
                    }
                    case RANDOM100 -> {
                        Optional<Song> s = songs.getRandomTop100Song();
                        if (s.isPresent()) {
                            yield this.songService.loadSong(s.get().getLink());
                        } else {
                            yield null;
                        }
                    }
                    case PLAYLIST -> {
                        int id = this.permutation.getNext();
                        assert this.playlistData != null;
                        yield this.playlistService.loadPlaylistEntry(this.playlistData.getCanonicalURL(), id);
                    }
                };

                if (song == null) {
                    return null;
                }

                if (lockedSongRepository.countByUrl(song.getCanonicalURL()) > 0) {
                    logger.debug("Gapcloser generated locked Song");
                    continue;
                }

                logger.info(String.format("Gapcloser generated Song %s", song));

                return song;
            } catch (SongNotFound e) {
                logger.debug("Gapcloser generated invalid Song", e);
            } catch (IOException | PlaylistNotFound e) {
                logger.error("Error loading Gapcloser Song", e);
            }
        }

        logger.error("Loading Song Failed five times");
        return null;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        this.save();

        if (this.stateService.getState().getState() == StateService.StateData.State.WAITING_FOR_SONGS && mode != Mode.OFF) {
            this.clientService.notifynewSong();
        }
    }

    private void updateHistory(@Nullable PlaylistData oldData) {
        assert this.playlistData != null;
        this.playlistHistory.removeIf(historyEntry -> historyEntry.getUrl().equals(this.playlistData.getCanonicalURL()));

        if (oldData != null) {
            this.playlistHistory.removeIf(historyEntry -> historyEntry.getUrl().equals(oldData.getCanonicalURL()));
            this.playlistHistory.addFirst(new GapcloserConfigDTO.HistoryEntry(
                    oldData.getTitle(),
                    oldData.getCanonicalURL()
            ));
        }


        if (this.playlistHistory.size() > 10) {
            this.playlistHistory.removeLast();
        }
    }

    public GapcloserConfigDTO getState() {
        if (this.playlistData != null) {
            return new GapcloserConfigDTO(this.playlistData.getCanonicalURL(), this.playlistData.getTitle(), this.getMode(), this.getPlaylistHistory().toArray(new GapcloserConfigDTO.HistoryEntry[0]));
        } else {
            return new GapcloserConfigDTO("", "", this.getMode(), this.getPlaylistHistory().toArray(new GapcloserConfigDTO.HistoryEntry[0]));
        }
    }

    public void setPlaylistFromUrl(String playlistURL) throws PlaylistNotFound, IOException {
        // If the playlist is already set, we don't need to do anything
        if (this.playlistData != null && playlistURL.equals(this.playlistData.getCanonicalURL()) || playlistURL.isBlank()) {
            return;
        }

        this.loadPlaylistData(playlistURL);
    }

    private static class Permutationhelper {
        private int p;
        private final List<Integer> list;

        public Permutationhelper(int size) {
            this.p = 0;
            this.list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                this.list.add(i);
            }
            Collections.shuffle(this.list);
        }

        public int getNext() {
            if (p >= this.list.size()) {
                p = 0;
                Collections.shuffle(this.list);
            }
            return this.list.get(this.p++);
        }
    }
}
