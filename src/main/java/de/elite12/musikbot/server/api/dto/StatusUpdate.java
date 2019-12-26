package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.Song;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdate {
    private String status;
    private String songtitle;
    private String songlink;
    private short volume;
    private int playlistdauer;
    private ArrayList<Song> playlist;
}
