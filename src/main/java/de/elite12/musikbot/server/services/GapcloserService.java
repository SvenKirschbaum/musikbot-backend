package de.elite12.musikbot.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import de.elite12.musikbot.server.api.dto.GapcloserDTO;
import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.data.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.data.entity.Setting;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SettingRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.shared.util.SongIDParser;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    private String playlistURL = "";
    @Getter
    private String playlistName = "";
    @Getter
    private List<GapcloserDTO.HistoryEntry> playlistHistory = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Permutationhelper permutation;

    @Autowired
    private SettingRepository settings;

    @Autowired
    private SongRepository songs;

    @Autowired
    private YouTubeService youtube;

    @Autowired
    private SpotifyService spotifyService;

    @Autowired
    private LockedSongRepository lockedSongRepository;

    @Autowired
    private StateService stateService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;


    private static final Logger logger = LoggerFactory.getLogger(GapcloserService.class);

    @PostConstruct
    public void postConstruct() {
        Optional<Setting> mode = settings.findById("gapcloser");
        Optional<Setting> playlist = settings.findById("playlist");
        Optional<Setting> playlistName = settings.findById("playlistName");
        Optional<Setting> playlistHistory = settings.findById("playlistHistory");

        mode.ifPresent(setting -> this.mode = Mode.valueOf(setting.getValue()));
        playlist.ifPresent(setting -> this.playlistURL = setting.getValue());
        playlistName.ifPresent(setting -> this.playlistName = setting.getValue());
        playlistHistory.ifPresent(setting -> {
            try {
                this.playlistHistory = objectMapper.readValue(setting.getValue(), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                logger.error("Error reading Playlist History", e);
            }
        });

        if (mode.isEmpty() || playlist.isEmpty() || playlistName.isEmpty() || playlistHistory.isEmpty()) {
            this.save();
        }

        createPermutation();
    }

    @SneakyThrows
    private void save() {
    	Setting modesetting = new Setting("gapcloser", this.mode.toString());
        Setting playlistsetting = new Setting("playlist", this.playlistURL);
        Setting playlistNameSetting = new Setting("playlistName", this.playlistName);
        Setting historySetting = new Setting("playlistHistory", this.objectMapper.writeValueAsString(this.playlistHistory));

        settings.saveAll(Arrays.asList(modesetting, playlistsetting, playlistNameSetting, historySetting));

    	logger.debug("Einstellung " + modesetting + " wurde gespeichert");
    	logger.debug("Einstellung " + playlistsetting + " wurde gespeichert");
        logger.debug("Einstellung " + playlistNameSetting + " wurde gespeichert");
        logger.debug("Einstellung " + historySetting + " wurde gespeichert");
    }

    public Song getnextSong() {
        try {
        	for(int i = 0; i < 5; i++) {
        		String url = selectCandidate();
        		if(url == null) {
    				return null;
    			}

        		UnifiedTrack ut;

        		try {
                    ut = UnifiedTrack.fromURL(url, youtube, spotifyService);
                }
        		catch(TrackNotAvailableException | InvalidURLException e) {
        			logger.debug("Gapcloser generated invalid Song",e);
        			continue;
        		}

                if (lockedSongRepository.countByUrl(ut.getLink()) > 0) {
                    logger.debug("Gapcloser generated locked Song");
                    continue;
                }

        		Song s = new Song();
        		s.setPlayed(true);
        		s.setInsertedAt(new Date());
        		s.setPlayedAt(new Date());
        		s.setLink(ut.getLink());
        		s.setTitle(ut.getTitle());
        		s.setDuration(ut.getDuration());

        		s = songs.save(s);


                logger.info(String.format("Gapcloser generated Song %s", s));

                return s;
        	}

        	logger.error("Loading Song Failed five times");
        	return null;
        } catch (IOException e) {
        	logger.error("Error loading Gapcloser Song", e);
        }
        return null;
    }

    private String selectCandidate() throws IOException {
    	switch (this.getMode()) {
			case OFF: {
				return null;
			}
			case RANDOM: {
				Optional<Song> s = songs.getRandomSong();
				return s.map(Song::getLink).orElse(null);
			}
			case RANDOM100: {
				Optional<Song> s = songs.getRandomTop100Song();
				return s.map(Song::getLink).orElse(null);
			}
			case PLAYLIST: {
                String pid = SongIDParser.getPID(this.getPlaylistURL());
                String said = SongIDParser.getSAID(this.getPlaylistURL());
                String spid = SongIDParser.getSPID(this.getPlaylistURL());
                int id = this.permutation.getNext();

                if (pid != null) {
                    int page = (int) Math.floor(id / 50.0);
                    PlaylistItemListResponse r = youtube.api().playlistItems()
                        .list(List.of("snippet","status")).setPlaylistId(pid).setMaxResults(50L)
                        .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                        .execute();
                    for (int i = 0; i < page; i++) {
                        r = youtube.api().playlistItems().list(List.of("snippet","status"))
                            .setPlaylistId(pid).setMaxResults(50L)
                            .setPageToken(r.getNextPageToken())
                            .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                            .execute();
                    }
                    PlaylistItem item = r.getItems().get(id % 50);
                    return "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId();
                } else if (spid != null) {
                    Track t = spotifyService.getTrackfromPlaylist(spid, id);
                    return "https://open.spotify.com/track/" + t.getId();
                } else if (said != null) {
                    TrackSimplified t = spotifyService.getTrackfromAlbum(said, id);
                    return "https://open.spotify.com/track/" + t.getId();
                }
			}
    	}
    	return null;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        this.save();

        if (this.stateService.getState().getState() == StateService.StateData.State.WAITING_FOR_SONGS && mode != Mode.OFF) {
            this.clientService.notifynewSong();
        }
    }

    public void setPlaylist(String playlistName, String playlistURL) {
        this.updateHistory(playlistURL);

        this.playlistName = playlistName;
        this.playlistURL = playlistURL;
        this.createPermutation();

        this.save();
    }

    private void updateHistory(String newPlaylistURL) {
        this.playlistHistory.removeIf(historyEntry -> historyEntry.getUrl().equals(newPlaylistURL));
        this.playlistHistory.removeIf(historyEntry -> historyEntry.getUrl().equals(this.playlistURL));

        if (!newPlaylistURL.equals(this.playlistURL)) {
            this.playlistHistory.addFirst(new GapcloserDTO.HistoryEntry(
                    this.playlistName,
                    this.playlistURL
            ));
        }

        if (this.playlistHistory.size() > 10) {
            this.playlistHistory.removeLast();
        }
    }

    public void setPlaylistFromUrl(String playlistURL) throws InvalidURLException {
        String pid = SongIDParser.getPID(playlistURL);
        String said = SongIDParser.getSAID(playlistURL);
        String spid = SongIDParser.getSPID(playlistURL);

        if (pid != null) {
            try {
                PlaylistListResponse playlistListResponse = this.youtube.api().playlists()
                        .list(List.of("contentDetails", "snippet"))
                        .setId(Collections.singletonList(pid))
                        .execute();

                if (playlistListResponse.getItems().isEmpty()) {
                    throw new InvalidURLException("Playlist not found");
                }

                com.google.api.services.youtube.model.Playlist playlist = playlistListResponse.getItems().getFirst();

                if (playlist.getContentDetails().getItemCount() == 0) {
                    throw new InvalidURLException("Playlist contains no Songs");
                }

                String playlistName = String.format("[%s] %s", playlist.getSnippet().getChannelTitle(), playlist.getSnippet().getTitle());
                this.setPlaylist(playlistName, "https://www.youtube.com/playlist?list=" + pid);
            } catch (IOException e) {
                throw new InvalidURLException("Error loading Playlist");
            }
        } else if (said != null) {
            Album album = this.spotifyService.getAlbum(said);

            if (album == null) {
                throw new InvalidURLException("Unable to load Spotify Album");
            }

            String playlistName = String.format(
                    "[%s] %s",
                    Arrays.stream(album.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")),
                    album.getName()
            );
            this.setPlaylist(playlistName, "https://open.spotify.com/album/" + said);
        } else if (spid != null) {
            Playlist playlist = this.spotifyService.getPlaylist(spid);

            if (playlist == null) {
                throw new InvalidURLException("Unable to load Spotify Playlist");
            }

            String playlistName = String.format("[%s] %s", playlist.getOwner().getDisplayName(), playlist.getName());
            this.setPlaylist(playlistName, "https://open.spotify.com/playlist/" + spid);
        } else {
            throw new InvalidURLException("Provided URL does not correspond to a known Playlist URL Format");
        }
    }

    private void createPermutation() {
        String pid = SongIDParser.getPID(this.getPlaylistURL());
        String spid = SongIDParser.getSPID(this.getPlaylistURL());
        String said = SongIDParser.getSAID(this.getPlaylistURL());
        if (pid != null) {
            try {
            	PlaylistItemListResponse r = youtube.api().playlistItems().list(Collections.singletonList("snippet"))
                        .setPlaylistId(pid).setMaxResults(1L).setFields("pageInfo/totalResults")
                        .execute();
                this.permutation = new Permutationhelper(r.getPageInfo().getTotalResults());
            } catch (IOException e) {
                logger.error("Error loading Playlist count", e);
            }
        } else if (spid != null) {
            this.permutation = new Permutationhelper(spotifyService.getPlaylistlength(spid));
        } else if (said != null) {
            this.permutation = new Permutationhelper(spotifyService.getAlbumlength(said));
        } else {
            logger.error("Playlist invalid");
        }
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
