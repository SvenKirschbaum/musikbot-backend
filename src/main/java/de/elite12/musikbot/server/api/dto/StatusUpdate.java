package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.Song;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdate {
    @Schema(description = "The current Status")
    private String status;
    @Schema(description = "The title of the current Song")
    private String songtitle;
    @Schema(description = "The link of the current Song")
    private String songlink;
    @Schema(description = "The current Volume")
    private short volume;
    @Schema(description = "The summed duration of the songs in the playlist")
    private int playlistdauer;
    @Schema(description = "The current playlist")
    private ArrayList<Song> playlist;
    @Schema(description = "The progress of the song")
    private SongProgress progress;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SongProgress {
        @Schema(description = "When the Song has been started or resumed")
        Instant start;
        @Schema(description = "The current Time")
        Instant current;
        @Schema(description = "The Duration of the Song")
        Duration duration;
        @Schema(description = "How long the Song has been played before the time transmitted in start")
        Duration prepausedDuration;
        @Schema(description = "If the Song is currently playing")
        boolean paused;
    }
}
