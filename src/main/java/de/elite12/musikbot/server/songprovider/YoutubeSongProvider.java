package de.elite12.musikbot.server.songprovider;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import de.elite12.musikbot.server.config.ServiceProperties;
import de.elite12.musikbot.server.data.songprovider.PlaylistData;
import de.elite12.musikbot.server.data.songprovider.SongData;
import de.elite12.musikbot.server.exceptions.songprovider.PlaylistNotFound;
import de.elite12.musikbot.server.exceptions.songprovider.SongNotFound;
import de.elite12.musikbot.server.interfaces.SongProvider;
import de.elite12.musikbot.shared.SongTypes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(value = "YoutubeSongProvider")
@ConditionalOnProperty(
        value = "musikbot.youtube.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class YoutubeSongProvider implements SongProvider {

    private static final Pattern YOUTUBE_SONG_URL_PATTERN = Pattern.compile("^(?:(?:https?:)?//)?(?:(?:www|m)\\.)?(?:youtube(?:-nocookie)?\\.com|youtu.be)/(?:[\\w\\-]*(?:\\?v=|\\?.*&v=)|embed/|e/|live/|v/)?([\\w\\-]+)(?:\\S+)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern YOUTUBE_PLAYLIST_URL_PATTERN = Pattern.compile("^(?:http|https)://www\\.youtube\\.com/.*list=([a-zA-Z0-9_-]{10,}).*$", Pattern.CASE_INSENSITIVE);
    private final YouTube youtube;
    private final Set<Integer> categories;

    public YoutubeSongProvider(ServiceProperties config) throws GeneralSecurityException, IOException {
        this.youtube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                new GsonFactory(),
                request -> {
                }
        )
                .setYouTubeRequestInitializer(
                        new YouTubeRequestInitializer(
                                config.getYoutube().getApikey()
                        )
                )
                .setApplicationName("e12-musikbot")
                .build();

        this.categories = config.getYoutube().getCategories();
    }

    @Override
    public boolean supportsSongUrl(String url) {
        return YOUTUBE_SONG_URL_PATTERN.matcher(url.trim()).matches();
    }

    private String getSongId(String url) {
        Matcher matcher = YOUTUBE_SONG_URL_PATTERN.matcher(url.trim());
        matcher.matches();
        return matcher.group(1);
    }

    @Override
    public boolean supportsPlaylistUrl(String url) {
        return YOUTUBE_PLAYLIST_URL_PATTERN.matcher(url.trim()).matches();
    }

    private String getPlaylistId(String url) {
        Matcher matcher = YOUTUBE_PLAYLIST_URL_PATTERN.matcher(url.trim());
        matcher.matches();
        return matcher.group(1);
    }

    @Override
    public SongData getSong(String url) throws IOException, SongNotFound {
        List<Video> items = this.youtube.videos().list(List.of("id", "status", "snippet", "contentDetails"))
                .setId(Collections.singletonList(this.getSongId(url)))
                .setFields(
                        "items/id,items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/duration,items/snippet/categoryId,items/snippet/title,items/contentDetails/regionRestriction,items/snippet/liveBroadcastContent")
                .execute().getItems();

        if (items == null || items.isEmpty()) {
            throw new SongNotFound("Video not found");
        }

        Video video = items.getFirst();

        if (!video.getStatus().getUploadStatus().equals("processed")) {
            throw new SongNotFound("Video is private or processing");
        }

        if (video.getSnippet().getLiveBroadcastContent() != null && !video.getSnippet().getLiveBroadcastContent().equals("none")) {
            throw new SongNotFound("Video is a live broadcast");
        }

        if (video.getContentDetails() != null) {
            if (video.getContentDetails().getRegionRestriction() != null) {
                if (video.getContentDetails().getRegionRestriction().getBlocked() != null) {
                    if (video.getContentDetails().getRegionRestriction().getBlocked().contains("DE")) {
                        throw new SongNotFound("Video is blocked in Germany");
                    }
                }
                if (video.getContentDetails().getRegionRestriction().getAllowed() != null) {
                    if (!video.getContentDetails().getRegionRestriction().getAllowed().contains("DE")) {
                        throw new SongNotFound("Video is not allowed in Germany");
                    }
                }
            }
        }

        assert video.getContentDetails() != null;
        SongData songData = new SongData(
                video.getId(),
                SongTypes.YOUTUBE_VIDEO,
                video.getSnippet().getTitle(),
                "https://www.youtube.com/watch?v=" + video.getId(),
                Duration.parse(video.getContentDetails().getDuration())
        );

        if (!this.categories.contains(Integer.parseInt(video.getSnippet().getCategoryId()))) {
            songData.setGetNonAdminRestrictions(List.of("Das Video ist in keiner der erlaubten Kategorien"));
        } else {
            songData.setGetNonAdminRestrictions(Collections.emptyList());
        }

        return songData;
    }

    @Override
    public PlaylistData getPlaylist(String url, boolean withSongs) throws PlaylistNotFound, IOException {
        String id = this.getPlaylistId(url);
        List<Playlist> playlists = this.youtube.playlists()
                .list(List.of("snippet", "contentDetails")).setId(Collections.singletonList(id)).setFields("items/snippet/title,items/id,items/contentDetails/itemCount")
                .execute().getItems();

        if (playlists == null || playlists.isEmpty()) {
            throw new PlaylistNotFound();
        }

        Playlist playlist = playlists.getFirst();

        PlaylistData.Entry[] playlistDataEntries;
        if (withSongs) {
            long pages = Math.min(8, (playlist.getContentDetails().getItemCount() / 50) + 1);

            List<PlaylistData.Entry> entries = new ArrayList<>();

            PlaylistItemListResponse r = this.youtube.playlistItems()
                    .list(List.of("snippet", "status")).setPlaylistId(id).setMaxResults(50L)
                    .setFields("items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                    .execute();

            for (int page = 0; page < pages; page++) {
                List<PlaylistItem> list = r.getItems();
                for (PlaylistItem item : list) {
                    PlaylistData.Entry e = new PlaylistData.Entry(
                            item.getSnippet().getTitle(),
                            "https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId()
                    );
                    entries.add(e);
                }
                if (page != pages - 1) {
                    r = this.youtube.playlistItems().list(List.of("snippet", "status"))
                            .setPlaylistId(id).setMaxResults(50L)
                            .setPageToken(r.getNextPageToken())
                            .setFields("items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                            .execute();
                }
            }
            playlistDataEntries = entries.toArray(new PlaylistData.Entry[0]);
        } else {
            playlistDataEntries = new PlaylistData.Entry[0];
        }

        return new PlaylistData(
                playlist.getSnippet().getTitle(),
                "https://www.youtube.com/playlist?list=%s".formatted(playlist.getId()),
                "Youtube Playlist",
                playlist.getContentDetails().getItemCount().intValue(),
                playlistDataEntries
        );
    }

    @Override
    public SongData getPlaylistEntry(String url, int index) throws IOException, PlaylistNotFound, SongNotFound {
        try {
            String id = this.getPlaylistId(url);
            int page = (int) Math.floor(index / 50.0);

            PlaylistItemListResponse r = this.youtube.playlistItems()
                    .list(List.of("snippet", "status")).setPlaylistId(id).setMaxResults(50L)
                    .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                    .execute();

            for (int i = 0; i < page; i++) {
                r = this.youtube.playlistItems().list(List.of("snippet", "status"))
                        .setPlaylistId(id).setMaxResults(50L)
                        .setPageToken(r.getNextPageToken())
                        .setFields("items/snippet/resourceId/videoId,items/snippet/position,nextPageToken,pageInfo")
                        .execute();
            }


            PlaylistItem item = r.getItems().get(index % 50);

            //PlaylistItem does not contain the duration, so we have to get the video
            return this.getSong("https://www.youtube.com/watch?v=" + item.getSnippet().getResourceId().getVideoId());
        } catch (IndexOutOfBoundsException e) {
            throw new SongNotFound();
        }
    }
}
