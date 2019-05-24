package de.elite12.musikbot.server.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.elite12.musikbot.server.api.dto.PlaylistDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;

import de.elite12.musikbot.server.api.dto.PlaylistDTO.Entry;

@Service
public class PlaylistImporterService {

    @Autowired
	private YouTubeService youtube;

    @Autowired
	private SpotifyService spotify;
	
	private final Logger logger = LoggerFactory.getLogger(PlaylistImporterService.class);

    public PlaylistDTO getyoutubePlaylist(String id) {
        try {
            logger.debug("Querying Youtube...");
            PlaylistDTO p = new PlaylistDTO();
            List<com.google.api.services.youtube.model.Playlist> plist = youtube.api().playlists()
                    .list("snippet,contentDetails").setId(id).setFields("items/snippet/title,items/id,items/contentDetails/itemCount")
                    .execute().getItems();
            if(plist == null) {
            	throw new IOException("Playlist not found");
            }
            com.google.api.services.youtube.model.Playlist yl = plist.get(0);
           	long pages = Math.min(8, (yl.getContentDetails().getItemCount()/50)+1);
           	
           	p.id = id;
            p.typ = "youtube";
            p.name = yl.getSnippet().getTitle();
            p.link = "http://www.youtube.com/playlist?list=" + p.id;
            
            List<Entry> entries = new ArrayList<>();
           	
            PlaylistItemListResponse r = youtube.api().playlistItems()
                    .list("snippet,status").setPlaylistId(id).setMaxResults(50L)
                    .setFields("items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                    .execute();
            for(int page = 0; page < pages; page++) {
            	List<PlaylistItem> list = r.getItems();
                for (PlaylistItem item : list) {
                    Entry e = new Entry();
                    e.link = "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId();
                    e.name = item.getSnippet().getTitle();
                    entries.add(e);
                }
           		if(page!=pages-1) {
           			r = youtube.api().playlistItems().list("snippet,status")
                            .setPlaylistId(id).setMaxResults(50L)
                            .setPageToken(r.getNextPageToken())
                            .setFields("items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                            .execute();
           		}
           	}
            p.songs = entries.toArray(new Entry[0]);
            return p;
        } catch (IOException e) {
            logger.error("Error loading Playlist", e);
            return null;
        }
    }

    public PlaylistDTO getspotifyPlaylist(String uid, String pid) {
        com.wrapper.spotify.model_objects.specification.Playlist sp = spotify.getPlaylist(uid, pid);
        if (sp == null) {
            return null;
        }
        PlaylistDTO p = new PlaylistDTO();
        p.id = pid;
        p.typ = "spotifyplaylist";
        p.name = sp.getName();
        p.link = "https://open.spotify.com/user/" + uid + "/playlist/" + pid;
        
        List<Entry> entries = new ArrayList<>();
        
        for(int page = 0; page < Math.min(4,Math.ceil(sp.getTracks().getTotal()/100.0));page++) {
        	Paging<PlaylistTrack> list = spotify.getPlaylistTracks(sp, page);
        	if(list == null) return null;
        	for (int i = 0;i<list.getItems().length;i++) {
        		PlaylistTrack t = list.getItems()[i];
                Entry e = new Entry();
                e.link = "https://open.spotify.com/track/" + t.getTrack().getId();
                e.name = "[" + t.getTrack().getArtists()[0].getName() + "] " + t.getTrack().getName();
                entries.add(e);
            }
        }
        p.songs = entries.toArray(new Entry[0]);
        return p;
    }

    public PlaylistDTO getspotifyAlbum(String said) {
        Album a = spotify.getAlbum(said);
        if (a == null) {
            return null;
        }
        PlaylistDTO p = new PlaylistDTO();
        p.id = said;
        p.typ = "spotifyalbum";
        p.name = a.getName();
        p.link = "https://open.spotify.com/album/" + said;
        p.songs = new Entry[Math.min(200,a.getTracks().getTotal())];
        
        List<Entry> entries = new ArrayList<>();
        
        for(int page = 0; page < Math.min(4,Math.ceil(a.getTracks().getTotal()/100.0));page++) {
        	Paging<TrackSimplified> list = spotify.getAlbumTracks(a, page);
        	if(list == null) return null;
        	for (int i = 0;i<list.getItems().length;i++) {
        		TrackSimplified t = list.getItems()[i];
                Entry e = new Entry();
                e.link = "https://open.spotify.com/track/" + t.getId();
                e.name = "[" + t.getArtists()[0].getName() + "] " + t.getName();
                entries.add(e);
            }
        }
        p.songs = entries.toArray(new Entry[0]);
        return p;
    }
}
