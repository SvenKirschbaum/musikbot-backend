package de.elite12.musikbot.backend.api.dto;

import de.elite12.musikbot.backend.services.GapcloserService;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GapcloserConfigDTO {

    private String playlist;
    private String playlistName;
    private GapcloserService.Mode mode;
    private HistoryEntry[] history;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class HistoryEntry {
        private String name;
        private String url;
    }
}
