package de.elite12.musikbot.server.data.repository;

import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends PagingAndSortingRepository<Song, Long>{

	@Modifying
	@Query(value = "UPDATE Song s SET s.userAuthor = NULL, s.guestAuthor = ?2 WHERE s.userAuthor = ?1")
	Integer replaceUserAuthor(User author, String guest);

	@Nullable
	@Query(value = "select * from song s where s.played = false order by s.sort asc limit 1", nativeQuery = true)
	Song getNextSong();
	
	@Nullable
	@Query(value = "select * from song s where s.played = true order by s.sort desc limit 1", nativeQuery = true)
	Song getLastSong();
	
	@Query(value = "select * from song s WHERE s.user_author != 30 order by RAND() limit 1", nativeQuery = true)
	Optional<Song> getRandomSong();
	
	@Query(value = "SELECT * FROM (SELECT * FROM song s WHERE s.user_author != 30 GROUP BY s.link ORDER BY COUNT(*) DESC LIMIT 100) as a ORDER BY RAND() LIMIT 1", nativeQuery = true)
	Optional<Song> getRandomTop100Song();
	
	Long countByPlayed(Boolean played);
	
	Long countByLinkAndPlayed(String link, boolean played);

	Long countByUserAuthor(User author);

	Long countByGuestAuthor(String guest);

    Long countByUserAuthorAndPlayed(User author, boolean played);

    Long countByGuestAuthorAndPlayed(String guest, boolean played);
	
	Long countByUserAuthorAndSkipped(User author, boolean skipped);
	
	Long countByGuestAuthorAndSkipped(String guest, boolean skipped);
	
	Iterable<Song> findByPlayedOrderBySort(boolean played);

	List<Song> findByGuestAuthor(String guest);
	
	@Query(value = "select title,link from song WHERE title LIKE concat('%', replace(replace(?1, '%', '\\\\%'), '_', '\\_'), '%') AND (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) GROUP BY link ORDER BY count(*) DESC LIMIT 10", nativeQuery = true)
	Iterable<Tuple> findSearchResult(String search);
	
	Page<Song> findByPlayedOrderBySortDesc(boolean played, Pageable pageable);
	
	@Query(value = "SELECT title,link,COUNT(*) FROM song WHERE (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) GROUP BY title,link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery=true)
	Iterable<Tuple> findTopMostPlayed();
	
	@Query(value = "SELECT title,link,COUNT(*) FROM song WHERE skipped = TRUE AND (USER_AUTHOR != 30 OR USER_AUTHOR IS NULL) GROUP BY title,link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery=true)
	Iterable<Tuple> findTopMostSkipped();
	
	@Query(value = "SELECT u.name,COUNT(*) FROM song s LEFT JOIN user u ON s.user_author = u.id WHERE (USER_AUTHOR != 30) GROUP BY user_author ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery=true)
	Iterable<Tuple> findTopUser();
	
	@Query(value = "SELECT title,link,COUNT(*) as count FROM song s WHERE user_author = ?1 GROUP BY link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery = true)
	Iterable<TopResult> findTopByUser(User u);
	
	@Query(value = "SELECT title,link,COUNT(*) as count FROM song s WHERE guest_author = ?1 GROUP BY link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery = true)
	Iterable<TopResult> findTopByGuest(String u);
	
	@Query(value = "SELECT title,link,COUNT(*) as count FROM song s WHERE user_author = ?1 AND skipped = true GROUP BY link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery = true)
	Iterable<TopResult> findTopSkippedByUser(User u);
	
	@Query(value = "SELECT title,link,COUNT(*) as count FROM song s WHERE guest_author = ?1 AND skipped = true GROUP BY link ORDER BY COUNT(*) DESC LIMIT 10", nativeQuery = true)
	Iterable<TopResult> findTopSkippedByGuest(String u);
	
	@Query(value = "SELECT id,title,link FROM song s WHERE user_author = ?1 ORDER BY id DESC LIMIT 10", nativeQuery = true)
	Iterable<RecentResult> findRecentByUser(User u);
	
	@Query(value = "SELECT id,title,link FROM song s WHERE guest_author = ?1 ORDER BY id DESC LIMIT 10", nativeQuery = true)
	Iterable<RecentResult> findRecentByGuest(String u);

	interface TopResult {
		String gettitle();
		String getlink();
		Long getcount();
	}

	interface RecentResult {
		Long getid();
		String gettitle();
		String getlink();
	}
}
