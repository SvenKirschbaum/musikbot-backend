package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.Song;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
