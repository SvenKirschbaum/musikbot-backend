package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.services.GapcloserService;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GapcloserDTO {
    private String playlist;
    private GapcloserService.Mode mode;
}
