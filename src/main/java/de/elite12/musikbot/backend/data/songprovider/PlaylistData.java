package de.elite12.musikbot.backend.data.songprovider;

import lombok.Data;

@Data
public class PlaylistData {
    private final String title;
    private final String canonicalURL;
    private final String type;
    private final int length;
    private final Entry[] entries;

    @Data
    public static class Entry {
        public final String name;
        public final String link;
    }
}
