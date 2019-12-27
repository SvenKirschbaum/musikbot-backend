package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.Song;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
