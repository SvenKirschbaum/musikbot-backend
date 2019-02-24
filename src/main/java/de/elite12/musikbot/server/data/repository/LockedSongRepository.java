package de.elite12.musikbot.server.data.repository;

import org.springframework.data.repository.CrudRepository;

import de.elite12.musikbot.server.data.entity.LockedSong;

public interface LockedSongRepository extends CrudRepository<LockedSong, Long> {
	public LockedSong findByUrl(String url);
	
	public Long countByUrl(String url);
}
