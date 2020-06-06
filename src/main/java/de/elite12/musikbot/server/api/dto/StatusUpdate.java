package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.Song;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(notes = "The current Status")
    private String status;
    @ApiModelProperty(notes = "The title of the current Song")
    private String songtitle;
    @ApiModelProperty(notes = "The link of the current Song")
    private String songlink;
    @ApiModelProperty(notes = "The current Volume")
    private short volume;
    @ApiModelProperty(notes = "The summed duration of the songs in the playlist")
    private int playlistdauer;
    @ApiModelProperty(notes = "The current playlist")
    private ArrayList<Song> playlist;
    @ApiModelProperty(notes = "The progress of the song")
    private SongProgress progress;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SongProgress {
        @ApiModelProperty(notes = "When the Song has been started or resumed")
        Instant start;
        @ApiModelProperty(notes = "The current Time")
        Instant current;
        @ApiModelProperty(notes = "The Duration of the Song")
        Duration duration;
        @ApiModelProperty(notes = "How long the Song has been played before the time transmitted in start")
        Duration prepausedDuration;
        @ApiModelProperty(notes = "If the Song is currently playing")
        boolean paused;
    }
}
