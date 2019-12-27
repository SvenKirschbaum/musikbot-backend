package de.elite12.musikbot.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class createSongResponse {
    @ApiModelProperty(notes = "If the Song has been added successfully")
    private boolean success;
    @ApiModelProperty(notes = "If adding the Song has triggered a warning")
    private boolean warn;
    @ApiModelProperty(notes = "The reason adding the Song failed, or triggered a warning")
    private String message;
}
