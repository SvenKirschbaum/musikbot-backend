package de.elite12.musikbot.server.services;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;

import de.elite12.musikbot.server.core.MusikbotServiceProperties;

@Service
public class SpotifyService {
	
	private final MusikbotServiceProperties config;

    private SpotifyApi api;

    private boolean authorized = false;
    
    private final Logger logger = LoggerFactory.getLogger(SpotifyService.class);

    @Autowired
    public SpotifyService(MusikbotServiceProperties config) {
        this.config = config;
    }

    @PostConstruct
    public void postConstruct() {
    	api = new SpotifyApi.Builder().setClientId(config.getSpotify().getId()).setClientSecret(config.getSpotify().getSecret()).build();
    }

    private synchronized void check() {
        if (authorized) {
            return;
        }
        
        ClientCredentialsRequest request = api.clientCredentials().build();
        try {
            ClientCredentials c = request.execute();
            api.setAccessToken(c.getAccessToken());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    authorized = false;
                }
            }, (long) (c.getExpiresIn() * 1000 * 0.8));
            authorized = true;
        } catch (IOException | SpotifyWebApiException e) {
            logger.error("Error refreshing Token", e);
        }
    }
    
    public Track getTrack(String sid) {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetTrackRequest r = api.getTrack(sid).build();
        try {
            return r.execute();
        } catch (IOException | SpotifyWebApiException e) {
            logger.error("Error reading Track", e);
            return null;
        }
    }
    
    public Track getTrackRaw(String sid) throws SpotifyWebApiException, IOException {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetTrackRequest r = api.getTrack(sid).build();
        return r.execute();
    }

    public Album getAlbum(String sid) {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetAlbumRequest r = api.getAlbum(sid).build();
        try {
            return r.execute();
        } catch (IOException | SpotifyWebApiException e) {
            logger.error("Error reading Album", e);
            return null;
        }
    }

    public Playlist getPlaylist(String uid, String sid) {
        if (sid == null || uid == null || sid.isEmpty() || uid.isEmpty()) {
            return null;
        }
        check();
        
        //TODO: Change according to new Playlist format
        //This has been rolled back by spotify
        //https://developer.spotify.com/community/news/2018/06/12/changes-to-playlist-uris/
        @SuppressWarnings("deprecation")
		GetPlaylistRequest r = api.getPlaylist(uid, sid).build();
        try {
            return r.execute();
        } catch (IOException | SpotifyWebApiException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }
    
    public Paging<PlaylistTrack> getPlaylistTracks(Playlist p, int page) {
    	check();
    	try {
    		return api.getPlaylistsTracks(p.getId()).offset(page*100).build().execute();
    	} catch (IOException | SpotifyWebApiException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }
    
    public Paging<TrackSimplified> getAlbumTracks(Album p, int page) {
    	check();
    	try {
    		return api.getAlbumsTracks(p.getId()).offset(page*100).build().execute();
    	} catch (IOException | SpotifyWebApiException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }

    public Track getTrackfromPlaylist(String uid, String sid, int id) {
        if (sid == null || uid == null || sid.isEmpty() || uid.isEmpty()) {
            return null;
        }
        check();
        //TODO: s.o.
        @SuppressWarnings("deprecation")
        GetPlaylistsTracksRequest r = api.getPlaylistsTracks(uid, sid).limit(1).offset(id).build();
        try {
        	Paging<PlaylistTrack> t = r.execute();
            return t.getItems()[0].getTrack();
        } catch (IOException | SpotifyWebApiException | ArrayIndexOutOfBoundsException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }

    public Integer getPlaylistlength(String uid, String sid) {
        if (sid == null || uid == null || sid.isEmpty() || uid.isEmpty()) {
            return null;
        }
        check();
        
        return this.getPlaylist(uid, sid).getTracks().getTotal();
    }

	public Integer getAlbumlength(String said) {
		if (said == null || said.isEmpty()) {
			return null;
		}
		check();
		
		return this.getAlbum(said).getTracks().getTotal();
	}

	public TrackSimplified getTrackfromAlbum(String said, int id) {
		if (said == null || said.isEmpty()) {
			return null;
		}
		check();
		
		GetAlbumsTracksRequest r = api.getAlbumsTracks(said).limit(1).offset(id).build();
		try {
        	Paging<TrackSimplified> t = r.execute();
            return t.getItems()[0];
        } catch (IOException | SpotifyWebApiException | ArrayIndexOutOfBoundsException e) {
            logger.error("Error reading Album", e);
            return null;
        }
	}
}
