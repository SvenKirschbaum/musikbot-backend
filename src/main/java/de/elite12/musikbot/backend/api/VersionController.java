package de.elite12.musikbot.backend.api;

import de.elite12.musikbot.backend.api.dto.VersionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/version")
public class VersionController {
    @Autowired(required = false)
    private BuildProperties build;

    @GetMapping
    public VersionDTO getVersion() {
        if (build == null) {
            return new VersionDTO("Development");
        }

        return new VersionDTO(build.getVersion());
    }
}
