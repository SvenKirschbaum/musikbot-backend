package de.elite12.musikbot.server.api.dto;

import lombok.ToString;

@ToString(exclude = {"songs"})
public class PlaylistDTO {
    public static class Entry {
        public String name;
        public String link;
    }

    public String id;
    public String link;
    public String typ;
    public String name;
    public Entry[] songs;
}
