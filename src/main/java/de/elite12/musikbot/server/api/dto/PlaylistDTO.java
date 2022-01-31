package de.elite12.musikbot.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = {"songs"})
public class PlaylistDTO {
    @Schema(description = "The Id of the playlist")
    public String id;
    @Schema(description = "The link to the playlist")
    public String link;
    @Schema(description = "The type of the playlist")
    public String typ;
    @Schema(description = "The name of the playlist")
    public String name;
    @Schema(description = "A List of Songs in the playlist. Maximum 400 entries")
    public Entry[] songs;

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        @Schema(description = "The name of the Song")
        public String name;
        @Schema(description = "The link of the Song")
        public String link;
    }
}
