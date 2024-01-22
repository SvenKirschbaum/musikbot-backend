package de.elite12.musikbot.server.songprovider;

import com.neovisionaries.i18n.CountryCode;
import de.elite12.musikbot.server.config.ServiceProperties;
import de.elite12.musikbot.server.data.songprovider.PlaylistData;
import de.elite12.musikbot.server.data.songprovider.SongData;
import de.elite12.musikbot.server.exceptions.songprovider.PlaylistNotFound;
import de.elite12.musikbot.server.exceptions.songprovider.SongNotFound;
import de.elite12.musikbot.server.interfaces.SongProvider;
import de.elite12.musikbot.shared.SongTypes;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(value = "SpotifySongProvider")
@ConditionalOnProperty(
        value = "musikbot.spotify.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SpotifySongProvider implements SongProvider {

    private static final Pattern SPOTIFY_SONG_URL_PATTERN = Pattern.compile("^(?:spotify:track:|(?:http|https)://(?:play|open)\\.spotify\\.com/(?:intl-[a-z]{2}/)?track/)([a-zA-Z0-9_]{22}).*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPOTIFY_PLAYLIST_URL_PATTERN = Pattern.compile("^(?:spotify:playlist:|(?:http|https)://(?:play|open)\\.spotify\\.com/(?:intl-[a-z]{2}/)?playlist/)([a-zA-Z0-9]{22}).*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPOTIFY_ALBUM_URL_PATTERN = Pattern.compile("^(?:spotify:album:|(?:http|https)://(?:play|open)\\.spotify\\.com/(?:intl-[a-z]{2}/)?album/)([a-zA-Z0-9]{22}).*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPOTIFY_ARTIST_URL_PATTERN = Pattern.compile("^(?:spotify:artist:|(?:http|https)://(?:play|open)\\.spotify\\.com/(?:intl-[a-z]{2}/)?artist/)([a-zA-Z0-9]{22}).*$", Pattern.CASE_INSENSITIVE);
    private final SpotifyApi spotifyApi;
    private final TaskScheduler scheduler;
    private final Logger logger = LoggerFactory.getLogger(SpotifySongProvider.class);

    public SpotifySongProvider(ServiceProperties config, @Qualifier("taskScheduler") TaskScheduler scheduler) {
        this.spotifyApi = new SpotifyApi.Builder().setClientId(config.getSpotify().getId()).setClientSecret(config.getSpotify().getSecret()).build();
        this.scheduler = scheduler;
        this.updateCredentials();
    }

    private void updateCredentials() {
        int delay = 5;
        try {
            ClientCredentialsRequest request = this.spotifyApi.clientCredentials().build();
            ClientCredentials c = request.execute();
            this.spotifyApi.setAccessToken(c.getAccessToken());
            delay = c.getExpiresIn() * 8 / 10;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error while updating Spotify Credentials", e);
        } finally {
            this.scheduler.schedule(this::updateCredentials, Instant.now().plus(Duration.ofSeconds(delay)));
        }
    }

    @Override
    public boolean supportsSongUrl(String url) {
        return SPOTIFY_SONG_URL_PATTERN.matcher(url.trim()).matches();
    }

    private String getSongId(String url) {
        Matcher matcher = SPOTIFY_SONG_URL_PATTERN.matcher(url.trim());
        if (!matcher.matches()) throw new IllegalArgumentException();
        return matcher.group(1);
    }

    @Override
    public boolean supportsPlaylistUrl(String url) {
        return SPOTIFY_PLAYLIST_URL_PATTERN.matcher(url.trim()).matches() || SPOTIFY_ALBUM_URL_PATTERN.matcher(url.trim()).matches() || SPOTIFY_ARTIST_URL_PATTERN.matcher(url.trim()).matches();
    }

    private String getPlaylistId(String url) {
        Matcher matcher = SPOTIFY_PLAYLIST_URL_PATTERN.matcher(url.trim());
        if (!matcher.matches()) throw new IllegalArgumentException();
        return matcher.group(1);
    }

    private String getAlbumId(String url) {
        Matcher matcher = SPOTIFY_ALBUM_URL_PATTERN.matcher(url.trim());
        if (!matcher.matches()) throw new IllegalArgumentException();
        return matcher.group(1);
    }

    private String getArtistId(String url) {
        Matcher matcher = SPOTIFY_ARTIST_URL_PATTERN.matcher(url.trim());
        if (!matcher.matches()) throw new IllegalArgumentException();
        return matcher.group(1);
    }

    @Override
    public SongData getSong(String url) throws IOException, SongNotFound {
        if (!this.supportsSongUrl(url)) {
            throw new IllegalArgumentException();
        }

        GetTrackRequest r = this.spotifyApi.getTrack(this.getSongId(url)).market(CountryCode.DE).build();
        try {
            Track track = r.execute();

            if (!track.getIsPlayable() && !"explicit".equals(track.getRestrictions().getReason())) {
                throw new SongNotFound("Track is not playable: %s".formatted(track.getRestrictions().getReason()));
            }

            return new SongData(
                    track.getId(),
                    SongTypes.SPOTIFY_TRACK,
                    "[%s] %s".formatted(track.getArtists()[0].getName(), track.getName()),
                    track.getExternalUrls().get("spotify"),
                    Duration.ofMillis(track.getDurationMs())
            );
        } catch (SpotifyWebApiException | ParseException e) {
            logger.error("Error loading Track", e);
            throw new SongNotFound(e);
        }
    }

    @Override
    public PlaylistData getPlaylist(String url, boolean withSongs) throws IOException, PlaylistNotFound {
        if (SPOTIFY_PLAYLIST_URL_PATTERN.matcher(url.trim()).matches()) {
            return this.getSpotifyPlaylist(this.getPlaylistId(url), withSongs);
        } else if (SPOTIFY_ALBUM_URL_PATTERN.matcher(url.trim()).matches()) {
            return this.getSpotifyAlbum(this.getAlbumId(url), withSongs);
        } else if (SPOTIFY_ARTIST_URL_PATTERN.matcher(url.trim()).matches()) {
            return this.getSpotifyArtist(this.getArtistId(url), withSongs);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public SongData getPlaylistEntry(String url, int index) throws IOException, PlaylistNotFound, SongNotFound {
        if (SPOTIFY_PLAYLIST_URL_PATTERN.matcher(url.trim()).matches()) {
            return this.getSpotifyPlaylistEntry(this.getPlaylistId(url), index);
        } else if (SPOTIFY_ALBUM_URL_PATTERN.matcher(url.trim()).matches()) {
            return this.getSpotifyAlbumEntry(this.getAlbumId(url), index);
        } else if (SPOTIFY_ARTIST_URL_PATTERN.matcher(url.trim()).matches()) {
            return this.getSpotifyArtistEntry(this.getArtistId(url), index);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private PlaylistData getSpotifyPlaylist(String pid, boolean withSongs) throws PlaylistNotFound, IOException {
        try {
            Playlist playlist = this.spotifyApi.getPlaylist(pid).market(CountryCode.DE).fields("external_urls,name,tracks.total,owner.display_name").build().execute();

            PlaylistData.Entry[] playlistDataEntries = null;
            if (withSongs) {
                List<PlaylistData.Entry> entries = new ArrayList<>();

                for (int page = 0; page < Math.min(4, Math.ceil(playlist.getTracks().getTotal() / 100.0)); page++) {
                    Paging<PlaylistTrack> tracks = this.spotifyApi.getPlaylistsItems(pid)
                            .fields("items(track(type,name,external_urls,artists(name)))")
                            .market(CountryCode.DE)
                            .offset(page * 100)
                            .build()
                            .execute();

                    entries.addAll(
                            Arrays.stream(tracks.getItems())
                                    .map(PlaylistTrack::getTrack)
                                    .map(t -> (Track) t)
                                    .map(
                                            track ->
                                                    new PlaylistData.Entry(
                                                            "[%s] %s".formatted(track.getArtists()[0].getName(), track.getName()),
                                                            track.getExternalUrls().get("spotify")
                                                    )
                                    )
                                    .toList()
                    );
                }

                playlistDataEntries = entries.toArray(new PlaylistData.Entry[0]);
            }

            return new PlaylistData(
                    "[%s] %s".formatted(playlist.getOwner().getDisplayName(), playlist.getName()),
                    playlist.getExternalUrls().get("spotify"),
                    "Spotify Playlist",
                    playlist.getTracks().getTotal(),
                    playlistDataEntries
            );
        } catch (ParseException | SpotifyWebApiException e) {
            logger.error("Error loading Playlist", e);
            throw new PlaylistNotFound(e);
        }
    }

    private SongData getSpotifyPlaylistEntry(String pid, int index) throws PlaylistNotFound, IOException, SongNotFound {
        try {
            Paging<PlaylistTrack> tracks = this.spotifyApi.getPlaylistsItems(pid)
                    .fields("items(track(id,type,name,external_urls,duration_ms,artists(name)))")
                    .market(CountryCode.DE)
                    .offset(index)
                    .limit(1)
                    .build()
                    .execute();

            if (tracks.getItems().length == 0) {
                throw new SongNotFound();
            }

            Track track = (Track) tracks.getItems()[0].getTrack();

            return new SongData(
                    track.getId(),
                    SongTypes.SPOTIFY_TRACK,
                    "[%s] %s".formatted(track.getArtists()[0].getName(), track.getName()),
                    track.getExternalUrls().get("spotify"),
                    Duration.ofMillis(track.getDurationMs())
            );
        } catch (ParseException | SpotifyWebApiException e) {
            logger.error("Error loading Playlist", e);
            throw new PlaylistNotFound(e);
        }
    }

    private PlaylistData getSpotifyAlbum(String aid, boolean withSongs) throws PlaylistNotFound, IOException {
        try {
            Album album = this.spotifyApi.getAlbum(aid).market(CountryCode.DE).build().execute();

            PlaylistData.Entry[] playlistDataEntries = null;
            if (withSongs) {
                List<PlaylistData.Entry> entries = new ArrayList<>();

                for (int page = 0; page < Math.min(4, Math.ceil(album.getTracks().getTotal() / 100.0)); page++) {
                    Paging<TrackSimplified> tracks = this.spotifyApi.getAlbumsTracks(aid)
                            .market(CountryCode.DE)
                            .offset(page * 100)
                            .build()
                            .execute();

                    entries.addAll(
                            Arrays.stream(tracks.getItems())
                                    .map(
                                            track ->
                                                    new PlaylistData.Entry(
                                                            "[%s] %s".formatted(track.getArtists()[0].getName(), track.getName()),
                                                            track.getExternalUrls().get("spotify")
                                                    )
                                    )
                                    .toList()
                    );
                }

                playlistDataEntries = entries.toArray(new PlaylistData.Entry[0]);
            }

            return new PlaylistData(
                    "[%s] %s".formatted(album.getArtists()[0].getName(), album.getName()),
                    album.getExternalUrls().get("spotify"),
                    "Spotify Album",
                    album.getTracks().getTotal(),
                    playlistDataEntries
            );
        } catch (ParseException | SpotifyWebApiException e) {
            logger.error("Error loading Playlist", e);
            throw new PlaylistNotFound(e);
        }
    }

    private SongData getSpotifyAlbumEntry(String aid, int index) throws PlaylistNotFound, IOException, SongNotFound {
        try {
            Paging<TrackSimplified> tracks = this.spotifyApi.getAlbumsTracks(aid)
                    .market(CountryCode.DE)
                    .offset(index)
                    .limit(1)
                    .build()
                    .execute();

            if (tracks.getItems().length == 0) {
                throw new SongNotFound();
            }

            TrackSimplified track = tracks.getItems()[0];

            return new SongData(
                    track.getId(),
                    SongTypes.SPOTIFY_TRACK,
                    "[%s] %s".formatted(track.getArtists()[0].getName(), track.getName()),
                    track.getExternalUrls().get("spotify"),
                    Duration.ofMillis(track.getDurationMs())
            );
        } catch (ParseException | SpotifyWebApiException e) {
            logger.error("Error loading Playlist", e);
            throw new PlaylistNotFound(e);
        }
    }

    private PlaylistData getSpotifyArtist(String arid, boolean withSongs) throws PlaylistNotFound, IOException {
        try {
            Artist artist = this.spotifyApi.getArtist(arid).build().execute();

            Track[] tracks = this.spotifyApi.getArtistsTopTracks(arid, CountryCode.DE).build().execute();
            PlaylistData.Entry[] playlistDataEntries =
                    Arrays.stream(tracks)
                            .map(
                                    track ->
                                            new PlaylistData.Entry(
                                                    "[%s] %s".formatted(track.getArtists()[0].getName(), track.getName()),
                                                    track.getExternalUrls().get("spotify")
                                            )
                            )
                            .toArray(PlaylistData.Entry[]::new);

            return new PlaylistData(
                    "[%s] Top Tracks".formatted(artist.getName()),
                    artist.getExternalUrls().get("spotify"),
                    "Spotify Artist",
                    tracks.length,
                    playlistDataEntries
            );
        } catch (ParseException | SpotifyWebApiException e) {
            logger.error("Error loading Playlist", e);
            throw new PlaylistNotFound(e);
        }
    }

    private SongData getSpotifyArtistEntry(String arid, int index) throws PlaylistNotFound, IOException, SongNotFound {
        try {
            Track[] tracks = this.spotifyApi.getArtistsTopTracks(arid, CountryCode.DE).build().execute();

            if (tracks.length <= index) {
                throw new SongNotFound();
            }

            Track track = tracks[index];
            return new SongData(
                    track.getId(),
                    SongTypes.SPOTIFY_TRACK,
                    "[%s] %s".formatted(track.getArtists()[0].getName(), track.getName()),
                    track.getExternalUrls().get("spotify"),
                    Duration.ofMillis(track.getDurationMs())
            );
        } catch (ParseException | SpotifyWebApiException e) {
            logger.error("Error loading Playlist", e);
            throw new PlaylistNotFound(e);
        }
    }
}
