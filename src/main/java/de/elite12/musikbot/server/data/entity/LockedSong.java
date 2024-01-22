package de.elite12.musikbot.server.data.entity;

import de.elite12.musikbot.server.data.songprovider.SongData;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LockedSong {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    private String url;

	private String title;

    public void updateFromSongData(SongData songData) {
        this.url = songData.getCanonicalURL();
        this.title = songData.getTitle();
    }
}
