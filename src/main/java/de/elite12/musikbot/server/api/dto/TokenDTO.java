package de.elite12.musikbot.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenDTO {
    @ApiModelProperty(notes = "The Bearer Token")
    private String token;
}
