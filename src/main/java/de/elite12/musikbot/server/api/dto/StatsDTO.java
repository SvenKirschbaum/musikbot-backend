package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.api.v2.Stats;
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
    private List<TopSong> mostplayed;
    private List<TopSong> mostskipped;
    private List<TopUser> topuser;
    private List general;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSong {
        private String title;
        private String link;
        private Integer count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUser {
        private String name;
        private Integer count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralEntry {
        private String title;
        private Integer count;
    }
}
