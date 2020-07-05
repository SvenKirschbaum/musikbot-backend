package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.projection.TopSong;
import de.elite12.musikbot.server.data.projection.TopUser;
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
    private List<TopSong> mostPlayed;
    @ApiModelProperty(notes = "List of the most skipped Songs")
    private List<TopSong> mostSkipped;
    @ApiModelProperty(notes = "List of the most active Users")
    private List<TopUser> topUsers;
    @ApiModelProperty(notes = "General statistics")
    private List<GeneralEntry> general;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralEntry {
        @ApiModelProperty(notes = "The Title of the Statistik")
        private String title;
        @ApiModelProperty(notes = "The Value of the Statistik")
        private String value;

        public GeneralEntry(String user, long count) {
            this(user, Long.toString(count));
        }
    }
}
