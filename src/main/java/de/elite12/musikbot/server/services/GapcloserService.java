package de.elite12.musikbot.server.services;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.data.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.data.entity.Setting;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.LockedSongRepository;
import de.elite12.musikbot.server.data.repository.SettingRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.shared.util.SongIDParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import javax.annotation.PostConstruct;
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

    private Mode mode = Mode.OFF;
    private String playlist = "";

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


    private static final Logger logger = LoggerFactory.getLogger(GapcloserService.class);

    @PostConstruct
    public void postConstruct() {
        Optional<Setting> mode = settings.findById("gapcloser");
        Optional<Setting> playlist = settings.findById("playlist");
        boolean found = mode.isPresent() && playlist.isPresent();

        if(found) {
            this.mode = Mode.valueOf(mode.get().getValue());
            this.playlist = playlist.get().getValue();
        }
    	else {
    	    save();
        }

        createPermutation();
    }

    private void save() {
    	Setting modesetting = new Setting("gapcloser", this.mode.toString());
    	Setting playlistsetting = new Setting("playlist", this.playlist);

    	settings.saveAll(Arrays.asList(modesetting, playlistsetting));

    	logger.debug("Einstellung " + modesetting + " wurde gespeichert");
    	logger.debug("Einstellung " + playlistsetting + " wurde gespeichert");
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
				String pid = SongIDParser.getPID(this.getPlaylist());
				String said = SongIDParser.getSAID(this.getPlaylist());
                String spid = SongIDParser.getSPID(this.getPlaylist());
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

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        this.save();

        if (this.stateService.getState().getState() == StateService.StateData.State.WAITING_FOR_SONGS && mode != Mode.OFF) {
            this.clientService.notifynewSong();
        }
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
        this.createPermutation();
        this.save();
    }

    private void createPermutation() {
        String pid = SongIDParser.getPID(this.getPlaylist());
        String spid = SongIDParser.getSPID(this.getPlaylist());
        String said = SongIDParser.getSAID(this.getPlaylist());
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
