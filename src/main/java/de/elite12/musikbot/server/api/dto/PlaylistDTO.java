package de.elite12.musikbot.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

@ToString(exclude = {"songs"})
public class PlaylistDTO {
    public static class Entry {
        @ApiModelProperty(notes = "The name of the Song")
        public String name;
        @ApiModelProperty(notes = "The link of the Song")
        public String link;
    }
    @ApiModelProperty(notes = "The Id of the playlist")
    public String id;
    @ApiModelProperty(notes = "The link to the playlist")
    public String link;
    @ApiModelProperty(notes = "The type of the playlist")
    public String typ;
    @ApiModelProperty(notes = "The name of the playlist")
    public String name;
    @ApiModelProperty(notes = "A List of Songs in the playlist. Maximum 400 entries")
    public Entry[] songs;
}
