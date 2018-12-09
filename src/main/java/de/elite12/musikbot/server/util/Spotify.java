package de.elite12.musikbot.server.util;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

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

public class Spotify {
	private static final String clientId = "32f75446eac041a7bc8b35330d289558";
    private static final String clientSecret = "6506ca3b6d494c0f8c7c22b30218e9b4";

    private static final SpotifyApi api = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();

    private static boolean authorized = false;

    private static synchronized void check() {
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
                    Spotify.authorized = false;
                }
            }, (long) (c.getExpiresIn() * 1000 * 0.8));
            authorized = true;
        } catch (IOException | SpotifyWebApiException e) {
            Logger.getLogger(Spotify.class).error("Error refreshing Token", e);
        }
    }
    
    public static Track getTrack(String sid) {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetTrackRequest r = api.getTrack(sid).build();
        try {
            Track t = r.execute();
            //if (!t.getAvailableMarkets().contains("DE") && !t.getAvailableMarkets().isEmpty()) {
            //    t = null;
            //}
            return t;
        } catch (IOException | SpotifyWebApiException e) {
            Logger.getLogger(Spotify.class).error("Error reading Track", e);
            return null;
        }
    }
    
    public static Track getTrackRaw(String sid) throws SpotifyWebApiException, IOException {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetTrackRequest r = api.getTrack(sid).build();
        Track t = r.execute();
        return t;
    }

    public static Album getAlbum(String sid) {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetAlbumRequest r = api.getAlbum(sid).build();
        try {
            Album t = r.execute();
            return t;
        } catch (IOException | SpotifyWebApiException e) {
            Logger.getLogger(Spotify.class).error("Error reading Album", e);
            return null;
        }
    }

    public static Playlist getPlaylist(String uid, String sid) {
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
            Playlist t = r.execute();
            return t;
        } catch (IOException | SpotifyWebApiException e) {
            Logger.getLogger(Spotify.class).error("Error reading Playlist", e);
            return null;
        }
    }
    
    public static Paging<PlaylistTrack> getPlaylistTracks(Playlist p, int page) {
    	try {
    		return api.getPlaylistsTracks(p.getId()).offset(page*100).build().execute();
    	} catch (IOException | SpotifyWebApiException e) {
            Logger.getLogger(Spotify.class).error("Error reading Playlist", e);
            return null;
        }
    }
    
    public static Paging<TrackSimplified> getAlbumTracks(Album p, int page) {
    	try {
    		return api.getAlbumsTracks(p.getId()).offset(page*100).build().execute();
    	} catch (IOException | SpotifyWebApiException e) {
            Logger.getLogger(Spotify.class).error("Error reading Playlist", e);
            return null;
        }
    }

    public static Track getTrackfromPlaylist(String uid, String sid, int id) {
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
            Logger.getLogger(Spotify.class).error("Error reading Playlist", e);
            return null;
        }
    }

    public static Integer getPlaylistlength(String uid, String sid) {
        if (sid == null || uid == null || sid.isEmpty() || uid.isEmpty()) {
            return null;
        }
        check();
        
        return Spotify.getPlaylist(uid, sid).getTracks().getTotal();
    }

	public static Integer getAlbumlength(String said) {
		if (said == null || said.isEmpty()) {
			return null;
		}
		check();
		
		return Spotify.getAlbum(said).getTracks().getTotal();
	}

	public static TrackSimplified getTrackfromAlbum(String said, int id) {
		if (said == null || said.isEmpty()) {
			return null;
		}
		check();
		
		GetAlbumsTracksRequest r = api.getAlbumsTracks(said).limit(1).offset(id).build();
		try {
        	Paging<TrackSimplified> t = r.execute();
            return t.getItems()[0];
        } catch (IOException | SpotifyWebApiException | ArrayIndexOutOfBoundsException e) {
            Logger.getLogger(Spotify.class).error("Error reading Album", e);
            return null;
        }
	}
}
