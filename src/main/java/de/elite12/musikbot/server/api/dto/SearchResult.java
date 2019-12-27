package de.elite12.musikbot.server.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
@ApiModel(description = "The SeachResult ordered by the likeliness of the Entries")
public class SearchResult {
    @ApiModelProperty(notes = "The link to the Result")
    private String value;
    @ApiModelProperty(notes = "The name of the Result")
    private String label;
}
