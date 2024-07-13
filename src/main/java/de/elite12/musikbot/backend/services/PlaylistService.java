package de.elite12.musikbot.backend.services;

import de.elite12.musikbot.backend.data.songprovider.PlaylistData;
import de.elite12.musikbot.backend.data.songprovider.SongData;
import de.elite12.musikbot.backend.exceptions.songprovider.PlaylistNotFound;
import de.elite12.musikbot.backend.exceptions.songprovider.SongNotFound;
import de.elite12.musikbot.backend.interfaces.SongProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Service
@Cacheable("playlist")
public class PlaylistService {

    private final SongProvider[] provider;

    private final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    public PlaylistService(ListableBeanFactory listableBeanFactory) {
        this.provider = listableBeanFactory.getBeansOfType(SongProvider.class).values().toArray(SongProvider[]::new);
    }

    public PlaylistData loadPlaylist(String url) throws PlaylistNotFound, IOException {
        return loadPlaylist(url, false);
    }

    public PlaylistData loadPlaylist(String url, boolean withSongs) throws PlaylistNotFound, IOException {
        SongProvider provider = getPlaylistProvider(url);

        return provider.getPlaylist(url, withSongs);
    }

    public SongData loadPlaylistEntry(String url, int index) throws IOException, PlaylistNotFound, SongNotFound {
        SongProvider provider = getPlaylistProvider(url);

        return provider.getPlaylistEntry(url, index);
    }

    private SongProvider getPlaylistProvider(String url) throws PlaylistNotFound {
        Optional<SongProvider> optionalSongProvider = Arrays.stream(this.provider).filter(p -> p.supportsPlaylistUrl(url)).findFirst();

        if (optionalSongProvider.isEmpty()) {
            logger.debug("No Provider found for URL {}", url);
            throw new PlaylistNotFound("No Provider found for URL");
        }

        return optionalSongProvider.get();
    }
}
