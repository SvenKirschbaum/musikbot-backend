package de.elite12.musikbot.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateSongResponseDTO {
    @Schema(description = "If the Song has been added successfully")
    private boolean success;
    @Schema(description = "If adding the Song has triggered a warning")
    private boolean warn;
    @Schema(description = "The reason adding the Song failed, or triggered a warning")
    private String message;
}
