package de.elite12.musikbot.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    @Schema(description = "If the Request was successful")
    private boolean success;
    @Schema(description = "The Error Message if the Request failed")
    private String error;
    @Schema(description = "A Bearer Authentication Token if the Request was successful")
    private String token;
}
