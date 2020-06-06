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
public class TopUser {
    @ApiModelProperty(notes = "Name of the User")
    private String name;
    @ApiModelProperty(notes = "How many Songs the User requested")
    private Long count;
}
