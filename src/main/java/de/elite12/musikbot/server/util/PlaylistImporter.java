package de.elite12.musikbot.server.util;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.api.services.youtube.model.PlaylistItem;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.util.PlaylistImporter.Playlist.Entry;

public class PlaylistImporter {
    public static class Playlist {
        public static class Entry {
            public String name;
            public String link;
        }

        public String id;
        public String link;
        public String typ;
        public String name;
        public Entry[] entrys;
    }

    public static Playlist getyoutubePlaylist(String id) {
        try {
            Logger.getLogger(PlaylistImporter.class).debug("Querying Youtube...");
            List<PlaylistItem> list = Controller.getInstance().getYouTube().playlistItems().list("snippet,status")
                    .setKey(Controller.key).setPlaylistId(id).setMaxResults(50L)
                    .setFields("items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position").execute()
                    .getItems();
            if (list == null) {
                throw new IOException("Playlist not available");
            }
            Playlist p = new Playlist();
            com.google.api.services.youtube.model.Playlist meta = Controller.getInstance().getYouTube().playlists()
                    .list("snippet").setKey(Controller.key).setId(id).setFields("items/snippet/title,items/id")
                    .execute().getItems().get(0);
            p.id = id;
            p.typ = "youtube";
            p.name = meta.getSnippet().getTitle();
            p.link = "http://www.youtube.com/playlist?list=" + p.id;
            p.entrys = new Entry[list.size()];

            for (int i = 0; i < list.size(); i++) {
                PlaylistItem item = list.get(i);
                Entry e = new Entry();
                e.link = "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId();
                e.name = item.getSnippet().getTitle();
                p.entrys[i] = e;
            }
            return p;
        } catch (IOException e) {
            Logger.getLogger(PlaylistImporter.class).error("Error loading Playlist", e);
            return null;
        }
    }

    public static Playlist getspotifyPlaylist(String uid, String pid) {
        com.wrapper.spotify.model_objects.specification.Playlist sp = Spotify.getPlaylist(uid, pid);
        if (sp == null) {
            return null;
        }
        Playlist p = new Playlist();
        p.id = pid;
        p.typ = "spotifyplaylist";
        p.name = sp.getName();
        p.link = "https://open.spotify.com/user/" + uid + "/playlist/" + pid;
        p.entrys = new Entry[sp.getTracks().getTotal()];
        for (int i = 0; i < sp.getTracks().getTotal(); i++) {
            PlaylistTrack t = sp.getTracks().getItems()[i];
            Entry e = new Entry();
            e.link = "https://open.spotify.com/track/" + t.getTrack().getId();
            e.name = "[" + t.getTrack().getArtists()[0].getName() + "] " + t.getTrack().getName();
            p.entrys[i] = e;
        }
        return p;
    }

    public static Playlist getspotifyAlbum(String said) {
        Album a = Spotify.getAlbum(said);
        if (a == null) {
            return null;
        }
        Playlist p = new Playlist();
        p.id = said;
        p.typ = "spotifyalbum";
        p.name = a.getName();
        p.link = "https://open.spotify.com/album/" + said;
        p.entrys = new Entry[a.getTracks().getTotal()];
        for (int i = 0; i < a.getTracks().getTotal(); i++) {
            TrackSimplified t = a.getTracks().getItems()[i];
            Entry e = new Entry();
            e.link = "https://open.spotify.com/track/" + t.getId();
            e.name = "[" + t.getArtists()[0].getName() + "] " + t.getName();
            p.entrys[i] = e;
        }
        return p;
    }
}
