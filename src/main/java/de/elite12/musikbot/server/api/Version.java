package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.api.dto.VersionDTO;
import de.elite12.musikbot.server.core.MusikbotServiceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/version")
public class Version {
    @Autowired
    private MusikbotServiceProperties properties;

    @GetMapping
    public VersionDTO getVersion() {
        return new VersionDTO(properties.getVersion());
    }
}