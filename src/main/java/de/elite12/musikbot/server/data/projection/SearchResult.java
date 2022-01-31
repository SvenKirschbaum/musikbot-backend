package de.elite12.musikbot.server.data.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Schema(description = "The SeachResult ordered by the likeliness of the Entries")
public class SearchResult {
    @Schema(description = "The link to the Result")
    private String value;
    @Schema(description = "The name of the Result")
    private String label;
}
