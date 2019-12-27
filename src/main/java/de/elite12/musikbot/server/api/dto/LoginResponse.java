package de.elite12.musikbot.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    @ApiModelProperty(notes = "If the Request was successful")
    private boolean success;
    @ApiModelProperty(notes = "The Error Message if the Request failed")
    private String error;
    @ApiModelProperty(notes = "A Bearer Authentication Token if the Request was successful")
    private String token;
}
