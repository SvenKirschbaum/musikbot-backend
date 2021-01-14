package de.elite12.musikbot.server.services;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import de.elite12.musikbot.server.config.ServiceProperties;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class SpotifyService {

    @Autowired
    private ServiceProperties config;

    private SpotifyApi api;

    private boolean authorized = false;

    private final Logger logger = LoggerFactory.getLogger(SpotifyService.class);

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
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error refreshing Token", e);
        }
    }
    
    public Track getTrack(String sid) {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetTrackRequest r = api.getTrack(sid).market(CountryCode.DE).build();
        try {
            return r.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error reading Track", e);
            return null;
        }
    }
    
    public Track getTrackRaw(String sid) throws SpotifyWebApiException, IOException, ParseException {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetTrackRequest r = api.getTrack(sid).market(CountryCode.DE).build();
        return r.execute();
    }

    public Album getAlbum(String sid) {
        if (sid == null || sid.isEmpty()) {
            return null;
        }
        check();
        GetAlbumRequest r = api.getAlbum(sid).market(CountryCode.DE).build();
        try {
            return r.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error reading Album", e);
            return null;
        }
    }

    public Playlist getPlaylist(String spid) {
        if (spid == null || spid.isEmpty()) {
            return null;
        }
        check();

        GetPlaylistRequest r = api.getPlaylist(spid).market(CountryCode.DE).build();
        try {
            return r.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }

    public Artist getArtist(String sarid) {
        if(sarid == null || sarid.isEmpty()) {
            return null;
        }
        check();

        GetArtistRequest r = api.getArtist(sarid).build();
        try {
            return r.execute();
        }
        catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error reading Artist", e);
            return null;
        }
    }

    public Track[] getArtistTracks(String sarid) {
        if(sarid == null || sarid.isEmpty()) {
            return null;
        }
        check();

        GetArtistsTopTracksRequest tracksRequest = api.getArtistsTopTracks(sarid, CountryCode.DE).build();
        try {
            return tracksRequest.execute();
        }
        catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error reading ArtistTracks", e);
            return null;
        }
    }
    
    public Paging<PlaylistTrack> getPlaylistTracks(Playlist p, int page) {
    	check();
    	try {
    	    return api.getPlaylistsItems(p.getId()).market(CountryCode.DE).offset(page*100).build().execute();
    	} catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }
    
    public Paging<TrackSimplified> getAlbumTracks(Album p, int page) {
    	check();
    	try {
    		return api.getAlbumsTracks(p.getId()).market(CountryCode.DE).offset(page*100).build().execute();
    	} catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }

    public Track getTrackfromPlaylist(String spid, int id) {
        if (spid == null || spid.isEmpty()) {
            return null;
        }
        check();

        GetPlaylistsItemsRequest r = api.getPlaylistsItems(spid).market(CountryCode.DE).limit(1).offset(id).build();
        try {
        	Paging<PlaylistTrack> t = r.execute();
            return (Track) t.getItems()[0].getTrack();
        } catch (IOException | SpotifyWebApiException | ArrayIndexOutOfBoundsException | ParseException e) {
            logger.error("Error reading Playlist", e);
            return null;
        }
    }

    public Integer getPlaylistlength(String spid) {
        if (spid == null || spid.isEmpty()) {
            return null;
        }
        check();
        
        return this.getPlaylist(spid).getTracks().getTotal();
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
		
		GetAlbumsTracksRequest r = api.getAlbumsTracks(said).market(CountryCode.DE).limit(1).offset(id).build();
		try {
        	Paging<TrackSimplified> t = r.execute();
            return t.getItems()[0];
        } catch (IOException | SpotifyWebApiException | ArrayIndexOutOfBoundsException | ParseException e) {
            logger.error("Error reading Album", e);
            return null;
        }
	}
}
