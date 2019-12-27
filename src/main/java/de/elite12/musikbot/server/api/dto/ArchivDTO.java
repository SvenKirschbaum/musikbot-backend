package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.Song;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArchivDTO {

    @Min(1)
    private int page;

    private int pages;

    private Song[] list;

}
