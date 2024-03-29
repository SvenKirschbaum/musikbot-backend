package de.elite12.musikbot.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@ToString(exclude = {"songs"})
@Data
public class PlaylistDTO {
    @Schema(description = "The link to the playlist")
    public String link;
    @Schema(description = "The type of the playlist")
    public String typ;
    @Schema(description = "The name of the playlist")
    public String name;
    @Schema(description = "A List of Songs in the playlist. Maximum 400 entries")
    public Entry[] songs;

    @AllArgsConstructor
    public static class Entry {
        @Schema(description = "The name of the Song")
        public String name;
        @Schema(description = "The link of the Song")
        public String link;
    }
}
