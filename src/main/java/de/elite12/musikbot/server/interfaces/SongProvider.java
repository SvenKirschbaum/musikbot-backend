package de.elite12.musikbot.server.interfaces;

import de.elite12.musikbot.server.data.songprovider.PlaylistData;
import de.elite12.musikbot.server.data.songprovider.SongData;
import de.elite12.musikbot.server.exceptions.songprovider.PlaylistNotFound;
import de.elite12.musikbot.server.exceptions.songprovider.SongNotFound;

import java.io.IOException;

public interface SongProvider {
    /**
     * This method is used to check if a given URL is a valid song URL for this provider.
     * The check is performed only syntactically, no network requests are made.
     *
     * @param url the URL to check
     * @return true if the URL is a valid song URL for this provider, false otherwise
     */
    boolean supportsSongUrl(String url);

    /**
     * This method is used to check if a given URL is a valid playlist URL for this provider.
     * The check is performed only syntactically, no network requests are made.
     *
     * @param url the URL to check
     * @return true if the URL is a valid playlist URL for this provider, false otherwise
     */
    boolean supportsPlaylistUrl(String url);

    /**
     * This method is used to load the SongData object for a given URL.
     *
     * @param url the URL to get the SongData for
     * @return a SongData object for the given URL
     * @throws SongNotFound if the URL is not a valid song URL for this provider
     */
    SongData getSong(String url) throws IOException, SongNotFound;

    /**
     * This method is used to load the PlaylistData object for a given URL.
     *
     * @param url       the URL to get the PlaylistData for
     * @param withSongs if true, the PlaylistData object should contain the songs of the playlist
     * @return a PlaylistData object for the given URL
     */
    PlaylistData getPlaylist(String url, boolean withSongs) throws PlaylistNotFound, IOException;

    /**
     * This method is used to load the SongData object for a given Playlist URL and index.
     *
     * @param url   the URL of the playlist
     * @param index the index of the song in the playlist
     * @return a SongData object for the given URL and index
     * @throws IOException      if an error occurs while loading the song
     * @throws PlaylistNotFound if the playlist at the given URL does not exist
     * @throws SongNotFound     if the song at the given index does not exist
     */
    SongData getPlaylistEntry(String url, int index) throws IOException, PlaylistNotFound, SongNotFound;
}
