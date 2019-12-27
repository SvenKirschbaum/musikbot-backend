package de.elite12.musikbot.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {
    @ApiModelProperty(notes = "List of the most played Songs")
    private List<TopSong> mostplayed;
    @ApiModelProperty(notes = "List of the most skipped Songs")
    private List<TopSong> mostskipped;
    @ApiModelProperty(notes = "List of the most active Users")
    private List<TopUser> topuser;
    @ApiModelProperty(notes = "General statistics")
    private List<GeneralEntry> general;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSong {
        @ApiModelProperty(notes = "Title of the Song")
        private String title;
        @ApiModelProperty(notes = "Link of the Song")
        private String link;
        @ApiModelProperty(notes = "How oftern the Song has been requested/skipped")
        private Integer count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUser {
        @ApiModelProperty(notes = "Name of the User")
        private String name;
        @ApiModelProperty(notes = "How many Songs the User requested")
        private Integer count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralEntry {
        @ApiModelProperty(notes = "The Title of the Statistik")
        private String title;
        @ApiModelProperty(notes = "The Value of the Statistik")
        private String value;
    }
}
