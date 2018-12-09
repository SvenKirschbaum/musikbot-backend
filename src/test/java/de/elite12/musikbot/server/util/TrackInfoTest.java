/**
 *
 */
package de.elite12.musikbot.server.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.wrapper.spotify.model_objects.specification.Track;


/**
 * @author sven
 *
 */
@RunWith(Parameterized.class)
public class TrackInfoTest {
    private String in;
    private Integer length;
    private String name;
    private String artist;

    public TrackInfoTest(String link, Integer expe, String expe2, String expe3) {
        this.in = link;
        this.length = expe;
        this.name = expe2;
        this.artist = expe3;
    }

    /**
     * Test method for {@link de.elite12.musikbot.shared.SongIDParser#vertifyTrack(java.lang.String)}.
     */
    @Test
    public final void testVertifyTrack() {
        Track track = Spotify.getTrack(this.in);
        if (track == null) {
            if (this.length != null) {
                fail("Track is null: " + this.in);
            }
        } else {
            assertEquals(this.length, ((Integer)(int)Math.round(track.getDurationMs().doubleValue() / 1000)));
            assertEquals(this.name, track.getName());
            assertEquals(this.artist, track.getArtists()[0].getName());
        }
    }

    @SuppressWarnings("rawtypes")
    @Parameters
    public static Collection input() {
        return Arrays.asList(new Object[][] { { null, null, null, null }, { "", null, null, null },
                { "asdblub", null, null, null }, { "0BCPKOYdS2jbQ8iyB56Zns", (308), "Clocks", "Coldplay" },
                { "60a0Rd6pjrkxjPbaKzXjfq", (217), "In The End", "Linkin Park" },
                { "5sNESr6pQfIhL3krM8CtZn", (206), "Numb / Encore", "JAY Z" },
                { "5xioIP2HexKl3QsI8JDlG8", (315), "No Other Plans", "Sunny Levine" },
                { "6COTbWO858qpfqxk0g4oYX", (202),
                        "5) He's A Pirate - Pirate Of The Carribbean - Launchpad Orchestral Live Remix",
                        "Giulio's Page" },
                { "5xioIPasd2HexKl3QsI8JDlG8", null, null, null }, { "5xioIP2HexKl3fQsI8JDlG8", null, null, null },
                { "5xioIP2fs4Kl3QsI8JDlG8", null, null, null } });
    }
}
