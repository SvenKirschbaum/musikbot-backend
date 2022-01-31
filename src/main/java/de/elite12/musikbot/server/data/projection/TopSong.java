package de.elite12.musikbot.server.data.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopSong {
    @Schema(description = "Title of the Song")
    private String title;
    @Schema(description = "Link of the Song")
    private String link;
    @Schema(description = "How often the Song has been requested/skipped")
    private Long count;
}
