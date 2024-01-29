package de.elite12.musikbot.backend.data.repository;

import de.elite12.musikbot.backend.data.entity.LockedSong;
import org.springframework.data.repository.CrudRepository;

public interface LockedSongRepository extends CrudRepository<LockedSong, Long> {
	Long countByUrl(String url);
}
