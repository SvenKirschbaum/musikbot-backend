package de.elite12.musikbot.backend.api.dto;

import de.elite12.musikbot.backend.services.GapcloserService;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GapcloserUpdateConfigDTO {

    private String playlist;
    private GapcloserService.Mode mode;
}
