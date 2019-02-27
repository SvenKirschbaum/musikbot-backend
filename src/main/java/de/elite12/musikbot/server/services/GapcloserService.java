package de.elite12.musikbot.server.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;

import de.elite12.musikbot.server.data.UnifiedTrack;
import de.elite12.musikbot.server.data.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.data.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.data.entity.Setting;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SettingRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.shared.SongIDParser;
import de.elite12.musikbot.shared.SongIDParser.SpotifyPlaylistHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

@Service
public class GapcloserService {

    public enum Mode {
        OFF,
        RANDOM100,
        RANDOM,
        PLAYLIST
    };

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
    private SpotifyService spotify;
    
    @Autowired
    private ClientService client;
    
    
    private static final Logger logger = LoggerFactory.getLogger(GapcloserService.class);

    @PostConstruct
    public void postConstruct() {
//    	boolean notfound = false;
//    	try {
//    		mode = Mode.valueOf(settings.findById("gapcloser").get().getValue());
//    	}
//    	catch(NoSuchElementException e) {notfound = true;}
//    	
//    	try {
//    		playlist = settings.findById("playlist").get().getValue();
//    	}
//    	catch(NoSuchElementException e) {notfound = true;}
//    	
//    	if(notfound) save();
//    	
//        createPermutation();
    }

    private void save() {
    	Setting modesetting = new Setting("gapcloser", this.mode.toString());
    	Setting playlistsetting = new Setting("playlist", this.playlist);
    	
    	settings.saveAll(Arrays.asList(new Setting[] {modesetting, playlistsetting}));
        
    	logger.debug("Einstellung " + modesetting + " wurde gespeichert");
    	logger.debug("Einstellung " + playlistsetting + " wurde gespeichert");
    }

    public Song getnextSong() {
        try {
        	for(int i = 0; i < 3; i++) {
        		String url = selectCandidate();
        		if(url == null) {
    				return null;
    			}
        		
        		UnifiedTrack ut;
        		
        		try {
        			ut = UnifiedTrack.fromURL(url,youtube,spotify);
        		}
        		catch(TrackNotAvailableException | InvalidURLException e) {
        			logger.debug("Generated invalid Song",e);
        			continue;
        		}
        		
        		Song s = new Song();
        		s.setPlayed(true);
        		s.setLink(ut.getLink());
        		s.setTitle(ut.getTitle());
        		s.setGuestAuthor("Automatisch");
        		s.setDuration(ut.getDuration());
        		
        		s = songs.save(s);
        		
    			
                
                logger.info("Gapcloser generated Song (ID: " + s.getId() + ") " + s.toString());
                
                return s;
        	}
        	
        	logger.error("Loading Song Failed three times");
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
				return s.isPresent() ? s.get().getLink() : null;
			}
			case RANDOM100: {
				Optional<Song> s = songs.getRandomTop100Song();
				return s.isPresent() ? s.get().getLink() : null;
			}
			case PLAYLIST: {
				String pid = SongIDParser.getPID(this.getPlaylist());
				String said = SongIDParser.getSAID(this.getPlaylist());
                SpotifyPlaylistHelper spid = SongIDParser.getSPID(this.getPlaylist());
                int id = this.permutation.getNext();

                if (pid != null) {
                    int page = (int) Math.floor(id / 50.0);
                    PlaylistItemListResponse r = youtube.api().playlistItems()
                        .list("snippet,status").setPlaylistId(pid).setMaxResults(50L)
                        .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                        .execute();
                    for (int i = 0; i < page; i++) {
                        r = youtube.api().playlistItems().list("snippet,status")
                            .setPlaylistId(pid).setMaxResults(50L)
                            .setPageToken(r.getNextPageToken())
                            .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                            .execute();
                    }
                    PlaylistItem item = r.getItems().get(id % 50);
                    return "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId();
                } else if (spid != null) {
                    Track t = spotify.getTrackfromPlaylist(spid.user, spid.pid, id);
                    return "https://open.spotify.com/track/" + t.getId();
                } else if (said != null) {
                	TrackSimplified t = spotify.getTrackfromAlbum(said, id);
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
        if(mode != Mode.OFF)
        	client.notifynewSong();
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
        SpotifyPlaylistHelper spid = SongIDParser.getSPID(this.getPlaylist());
        String said = SongIDParser.getSAID(this.getPlaylist());
        if (pid != null) {
            try {
            	PlaylistItemListResponse r = youtube.api().playlistItems().list("snippet")
                        .setPlaylistId(pid).setMaxResults(1L).setFields("pageInfo/totalResults")
                        .execute();
                this.permutation = new Permutationhelper(r.getPageInfo().getTotalResults());
            } catch (IOException e) {
                logger.error("Error loading Playlist count", e);
            }
        } else if (spid != null) {
            this.permutation = new Permutationhelper(spotify.getPlaylistlength(spid.user, spid.pid));
        } else if (said != null) {
        	this.permutation = new Permutationhelper(spotify.getAlbumlength(said));
        } else {
            logger.error("Playlist invalid");
        }
    }

    private class Permutationhelper {
        private int p;
        private List<Integer> list;

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
