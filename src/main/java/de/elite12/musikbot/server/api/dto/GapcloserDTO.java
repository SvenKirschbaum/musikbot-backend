package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.services.GapcloserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GapcloserDTO {
    private String playlist;
    private GapcloserService.Mode mode;
}
