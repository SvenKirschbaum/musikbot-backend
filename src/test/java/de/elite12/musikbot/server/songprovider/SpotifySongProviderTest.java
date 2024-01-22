package de.elite12.musikbot.server.songprovider;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class SpotifySongProviderTest {

    private SpotifySongProvider provider;

    public static Stream<Arguments> albumCases() {
        return Stream.of(
                Arguments.of("", false, null),
                Arguments.of("asdblub", false, null),
                Arguments.of("https://open.spotify.com/album/7n3QJc7TBOxXtlYh4Ssll8", true, "7n3QJc7TBOxXtlYh4Ssll8"),
                Arguments.of("spotify:album:7n3QJc7TBOxXtlYh4Ssll8", true, "7n3QJc7TBOxXtlYh4Ssll8"),
                Arguments.of("spotify:album:0PSrPgwe3rz2r4s7tS9GkH", true, "0PSrPgwe3rz2r4s7tS9GkH")
        );
    }

    public static Stream<Arguments> trackCases() {
        return Stream.of(
                Arguments.of("", false, null),
                Arguments.of("asdblub", false, null),
                Arguments.of("https://play.spotify.com/track/0BCPKOYdS2jbQ8iyB56Zns", true, "0BCPKOYdS2jbQ8iyB56Zns"),
                Arguments.of("https://play.spotify.com/track/60a0Rd6pjrkxjPbaKzXjfq", true, "60a0Rd6pjrkxjPbaKzXjfq"),
                Arguments.of("https://play.spotify.com/track/5sNESr6pQfIhL3krM8CtZn", true, "5sNESr6pQfIhL3krM8CtZn"),
                Arguments.of("http://play.spotify.com/track/60a0Rd6pjrkxjPbaKzXjfq", true, "60a0Rd6pjrkxjPbaKzXjfq"),
                Arguments.of("http://play.spotify.com/track/5sNESr6pQfIhL3krM8CtZn", true, "5sNESr6pQfIhL3krM8CtZn"),
                Arguments.of("spotify:track:5xioIP2HexKl3QsI8JDlG8", true, "5xioIP2HexKl3QsI8JDlG8"),
                Arguments.of("http://open.spotify.com/track/5pqF0qKSQCEq5mt5CFEXoq", true, "5pqF0qKSQCEq5mt5CFEXoq"),
                Arguments.of("https://open.spotify.com/track/6tBdTwcyyGq1HU3PXgZK82?si=rnSOfIimRraXvum_lzM3fw", true, "6tBdTwcyyGq1HU3PXgZK82")
        );
    }

    public static Stream<Arguments> playlistCases() {
        return Stream.of(
                Arguments.of("spotify:playlist:6Jp03bPknRPDM04qCzAsC7", true, "6Jp03bPknRPDM04qCzAsC7"),
                Arguments.of("https://open.spotify.com/playlist/6Jp03bPknRPDM04qCzAsC7", true, "6Jp03bPknRPDM04qCzAsC7"),
                Arguments.of("spotify:playlist:7Isff8LtbeEFmhqNlQGrpE", true, "7Isff8LtbeEFmhqNlQGrpE")
        );
    }

    @BeforeEach
    public void setUp() {
        this.provider = Mockito.mock(SpotifySongProvider.class, Mockito.CALLS_REAL_METHODS);
    }

    @ParameterizedTest
    @MethodSource("albumCases")
    public final void testAlbumParsing(String url, boolean valid, String albumId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        assertEquals(valid, provider.supportsPlaylistUrl(url));

        Method getAlbumId = provider.getClass().getDeclaredMethod("getAlbumId", String.class);
        getAlbumId.setAccessible(true);

        if (valid) {
            assertEquals(albumId, getAlbumId.invoke(provider, url));
        }
    }

    @ParameterizedTest
    @MethodSource("trackCases")
    public final void testTrackParsing(String url, boolean valid, String trackId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        assertEquals(valid, provider.supportsSongUrl(url));

        Method getSongId = provider.getClass().getDeclaredMethod("getSongId", String.class);
        getSongId.setAccessible(true);

        if (valid) {
            assertEquals(trackId, getSongId.invoke(provider, url));
        }
    }

    @ParameterizedTest
    @MethodSource("playlistCases")
    public final void testPlaylistParsing(String url, boolean valid, String pid) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        assertEquals(valid, provider.supportsPlaylistUrl(url));

        Method getPlaylistId = provider.getClass().getDeclaredMethod("getPlaylistId", String.class);
        getPlaylistId.setAccessible(true);

        if (valid) {
            assertEquals(pid, getPlaylistId.invoke(provider, url));
        }
    }
}
