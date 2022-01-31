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
public class TopUser {
    @Schema(description = "Name of the User")
    private String name;
    @Schema(description = "How many Songs the User requested")
    private Long count;
}
