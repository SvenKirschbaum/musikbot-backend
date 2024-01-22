package de.elite12.musikbot.server.data.songprovider;

import de.elite12.musikbot.shared.SongTypes;
import lombok.Data;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Data
public class SongData {
    private final String id;
    private final SongTypes type;
    private final String title;
    private final String canonicalURL;
    private final Duration duration;
    private List<String> getNonAdminRestrictions = Collections.emptyList();
}
