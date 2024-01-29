package de.elite12.musikbot.backend.songprovider;


import de.elite12.musikbot.backend.config.ServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {YoutubeSongProvider.class})
public class YoutubeSongProviderTest {

    @Autowired
    private YoutubeSongProvider provider;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceProperties properties;

    public static Stream<Arguments> videoCases() {
        return Stream.of(
                Arguments.of("", false, null),
                Arguments.of("asdblub", false, null),
                Arguments.of("http://www.youtube.com/watch?v=-wtIMTCHWuI", true, "-wtIMTCHWuI"),
                Arguments.of("http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1", true, "-wtIMTCHWuI"),
                Arguments.of("http://youtu.be/-wtIMTCHWuI", true, "-wtIMTCHWuI"),
                Arguments.of("http://www.youtube.com/embed/0zM3nApSvMg?rel=0", true, "0zM3nApSvMg"),
                Arguments.of("http://www.youtube.com/watch?v=0zM3nApSvMg&feature=feedrec_grec_index", true, "0zM3nApSvMg"),
                Arguments.of("http://www.youtube.com/watch?v=0zM3nApSvMg#t=0m10s", true, "0zM3nApSvMg"),
                Arguments.of("http://www.youtube.com/v/0zM3nApSvMg?fs=1&hl=en_US&rel=0", true, "0zM3nApSvMg"),
                Arguments.of("http://www.youtube.com/e/dQw4w9WgXcQ", true, "dQw4w9WgXcQ"),
                Arguments.of("http://www.youtube.com/?feature=player_embedded&v=dQw4w9WgXcQ", true, "dQw4w9WgXcQ")
        );
    }

    public static Stream<Arguments> playlistCases() {
        return Stream.of(
                Arguments.of("", false, null),
                Arguments.of("asdblub", false, null),
                Arguments.of("https://www.youtube.com/watch?v=OZq_T-EAg2M&list=PL6D4C31FFA7EBABB5", true, "PL6D4C31FFA7EBABB5"),
                Arguments.of("http://www.youtube.com/watch?v=OZq_T-EAg2M&feature=share&list=PL6D4C31FFA7EBABB5&index=1", true, "PL6D4C31FFA7EBABB5"),
                Arguments.of("https://www.youtube.com/playlist?list=PL6D4C31FFA7EBABB5", true, "PL6D4C31FFA7EBABB5"),
                Arguments.of("https://www.youtube.com/playlist?list=LLOxHw7X2HJxnljTc6Vmz1Mw", true, "LLOxHw7X2HJxnljTc6Vmz1Mw"),
                Arguments.of("https://www.youtube.com/watch?v=zOEcv7h4xvw&list=LLOxHw7X2HJxnljTc6Vmz1Mw", true, "LLOxHw7X2HJxnljTc6Vmz1Mw"),
                Arguments.of("https://www.youtube.com/playlist?list=PL2DcFzGCdNxEvB051ZfCS-LBczfVK_YPg", true, "PL2DcFzGCdNxEvB051ZfCS-LBczfVK_YPg"),
                Arguments.of("http://www.youtube.com/playlist?list=UU9YLd0REiXxLqQQH_CpJKZQ", true, "UU9YLd0REiXxLqQQH_CpJKZQ")
        );
    }

    @BeforeEach
    public void setUp() {
        Mockito.when(properties.getYoutube().getApikey()).thenReturn("APIKEYS");
        Mockito.when(properties.getYoutube().getCategories()).thenReturn(Collections.emptySet());
    }

    @ParameterizedTest
    @MethodSource("videoCases")
    public final void testVideoParsing(String url, boolean valid, String vid) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        assertEquals(valid, provider.supportsSongUrl(url));

        Method getSongId = provider.getClass().getDeclaredMethod("getSongId", String.class);
        getSongId.setAccessible(true);

        if (valid) {
            assertEquals(vid, getSongId.invoke(provider, url));
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
