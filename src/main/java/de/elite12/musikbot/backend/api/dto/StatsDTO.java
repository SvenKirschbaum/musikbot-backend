package de.elite12.musikbot.backend.api.dto;

import de.elite12.musikbot.backend.data.projection.TopSong;
import de.elite12.musikbot.backend.data.projection.TopUser;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "List of the most played Songs")
    private List<TopSong> mostPlayed;
    @Schema(description = "List of the most skipped Songs")
    private List<TopSong> mostSkipped;
    @Schema(description = "List of the most active Users")
    private List<TopUser> topUsers;
    @Schema(description = "General statistics")
    private List<GeneralEntry> general;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralEntry {
        @Schema(description = "The Title of the Statistik")
        private String title;
        @Schema(description = "The Value of the Statistik")
        private String value;

        public GeneralEntry(String user, long count) {
            this(user, Long.toString(count));
        }
    }
}
