package client;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.wrapper.spotify.models.Track;

import de.elite12.musikbot.shared.Util;

public class GenericTest {

    @Test
    public final void test() {
        assertTrue(0 == 2 - 2);
    }

    public static void main(String... strings) {
        Track t = Util.getTrackfromAlbum("7n3QJc7TBOxXtlYh4Ssll8", 1);
        System.out.println(t.getName());
    }

}
