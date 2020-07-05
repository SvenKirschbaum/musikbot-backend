package de.elite12.musikbot.server.data.projection;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopSong {
    @ApiModelProperty(notes = "Title of the Song")
    private String title;
    @ApiModelProperty(notes = "Link of the Song")
    private String link;
    @ApiModelProperty(notes = "How often the Song has been requested/skipped")
    private Long count;
}
