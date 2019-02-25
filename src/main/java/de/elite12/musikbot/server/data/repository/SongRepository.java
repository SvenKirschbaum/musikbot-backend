package de.elite12.musikbot.server.data.repository;

import java.util.Optional;

import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;

@Repository
public interface SongRepository extends PagingAndSortingRepository<Song, Long>{
	@Nullable
	@Query(value = "select * from song s where s.played = false order by s.sort asc limit 1", nativeQuery = true)
	Song getNextSong();
	
	@Nullable
	@Query(value = "select * from song s where s.played = true order by s.sort desc limit 1", nativeQuery = true)
	Song getLastSong();
	
	@Query(value = "select * from song s WHERE s.author != 'Automatisch' order by RAND() limit 1", nativeQuery = true)
	Optional<Song> getRandomSong();
	
	@Query(value = "SELECT * FROM (SELECT * FROM song WHERE s.author != 'Automatisch' GROUP BY s.link ORDER BY COUNT(*) DESC LIMIT 100) ORDER BY RAND() LIMIT 1", nativeQuery = true)
	Optional<Song> getRandomTop100Song();
	
	Long countByPlayed(Boolean played);
	
	Long countByLinkAndPlayed(String link, boolean played);
	
	Long countByUserAuthor(User author);
	
	Long countByGuestAuthor(String guest);
	
	Iterable<Song> findByPlayedOrderBySort(boolean played);
	
	@Query(value = "select title,link from song WHERE title LIKE concat('%', replace(replace(?1, '%', '\\\\%'), '_', '\\_'), '%') AND (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) GROUP BY link ORDER BY count(*) DESC LIMIT 10", nativeQuery = true)
	Iterable<Tuple> findSearchResult(String search);
	
	Page<Song> findByPlayedOrderBySortDesc(boolean played, Pageable pageable);
	
	@Query(value = "SELECT title,link,COUNT(*) FROM song WHERE (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) GROUP BY title,link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery=true)
	Iterable<Tuple> findTopMostPlayed();
	
	@Query(value = "SELECT title,link,COUNT(*) FROM song WHERE skipped = TRUE AND (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) GROUP BY title,link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery=true)
	Iterable<Tuple> findTopMostSkipped();
	
	@Query(value = "SELECT u.name,COUNT(*) FROM song s LEFT JOIN user u ON s.user_author = u.id WHERE (USER_AUTHOR != 30) GROUP BY user_author ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery=true)
	Iterable<Tuple> findTopUser();
	
}
