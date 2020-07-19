package de.elite12.musikbot.server.data.repository;

import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.projection.SearchResult;
import de.elite12.musikbot.server.data.projection.TopSong;
import de.elite12.musikbot.server.data.projection.TopUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends PagingAndSortingRepository<Song, Long>{

	@Modifying
	@Query(value = "UPDATE Song s SET s.userAuthor = NULL, s.guestAuthor = ?2 WHERE s.userAuthor = ?1")
	Integer replaceUserAuthor(User author, String guest);

	@Nullable
	@Query(value = "select * from song s where s.played = false order by s.sort limit 1", nativeQuery = true)
	Song getNextSong();

	@Nullable
	@Query(value = "select * from song s where s.played = true order by s.sort desc limit 1", nativeQuery = true)
	Song getLastSong();

	@Query(value = "select * from song s WHERE s.user_author != 30 order by RAND() limit 1", nativeQuery = true)
	Optional<Song> getRandomSong();

	@Query(value = "SELECT * FROM (SELECT * FROM song s WHERE s.user_author != 30 GROUP BY s.link ORDER BY COUNT(*) DESC LIMIT 100) as a ORDER BY RAND() LIMIT 1", nativeQuery = true)
	Optional<Song> getRandomTop100Song();

	@Query(value = "SELECT COALESCE(SUM(s.duration),0) FROM Song s")
	Long getCompleteDuration();

	@Query(value = "SELECT COALESCE(SUM(s.duration),0) FROM Song s WHERE s.userAuthor = ?1")
	Long getDurationByUserAuthor(User author);

	Long countByPlayed(Boolean played);

	Long countByLinkAndPlayed(String link, boolean played);

	Long countByUserAuthor(User author);

	Long countByGuestAuthor(String guest);

    Long countByUserAuthorAndPlayed(User author, boolean played);

    Long countByGuestAuthorAndPlayed(String guest, boolean played);

	Long countByUserAuthorAndSkipped(User author, boolean skipped);

	Long countByGuestAuthorAndSkipped(String guest, boolean skipped);

	Long countBySkipped(boolean skipped);

	Long countByUserAuthorNot(User author);

	Long countByUserAuthorNotAndSkipped(User author, boolean skipped);

	@Query(value = "SELECT COUNT(DISTINCT s.guestAuthor) FROM Song s")
	Long countGuests();

	Iterable<Song> findByPlayedOrderBySort(boolean played);

	List<Song> findByGuestAuthor(String guest);

	@Cacheable(cacheNames = "search", sync = true)
	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.SearchResult(s.link, s.title) FROM Song s WHERE s.title LIKE concat('%', replace(replace(?1, '%', '\\\\%'), '_', '\\_'), '%') AND (s.userAuthor != 30 OR s.userAuthor IS NULL) GROUP BY s.link ORDER BY count(s) DESC")
	Page<SearchResult> findSearchResult(String search, Pageable pageable);

	Page<Song> findByPlayedOrderBySortDesc(boolean played, Pageable pageable);

	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.TopSong(s.title, s.link, COUNT(s)) FROM Song s WHERE s.userAuthor != 30 OR s.userAuthor IS NULL GROUP BY s.title,s.link ORDER BY COUNT(s) DESC")
    Page<TopSong> findTopMostPlayed(Pageable pageable);

	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.TopSong(s.title, s.link, COUNT(s)) FROM Song s WHERE s.skipped = TRUE AND (s.userAuthor != 30 OR s.userAuthor IS NULL) GROUP BY s.title,s.link ORDER BY COUNT(s) DESC")
    Page<TopSong> findTopMostSkipped(Pageable pageable);

	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.TopUser(u.name, COUNT(s)) FROM Song s LEFT JOIN User u ON s.userAuthor = u.id WHERE s.userAuthor != 30 GROUP BY s.userAuthor ORDER BY COUNT(s) DESC")
    Page<TopUser> findTopUser(Pageable pageable);

	//@Cacheable(cacheNames = "stats", key = "'u-t-'.concat(#u.id)",sync = true)
	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.TopSong(s.title, s.link, COUNT(s)) FROM Song s WHERE s.userAuthor = ?1 GROUP BY s.link ORDER BY COUNT(s) DESC")
	Page<TopSong> findTopForUser(User u, Pageable pageable);

	@Cacheable(cacheNames = "stats", key = "'g-t-'.concat(#u)",sync = true)
	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.TopSong(s.title, s.link, COUNT(s)) FROM Song s WHERE s.guestAuthor = ?1 GROUP BY s.link ORDER BY COUNT(s) DESC")
	Page<TopSong> findTopForGuest(String u, Pageable pageable);

	@Cacheable(cacheNames = "stats", key = "'u-ts-'.concat(#u.id)",sync = true)
	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.TopSong(s.title, s.link, COUNT(s)) FROM Song s WHERE s.userAuthor = ?1 AND s.skipped = true GROUP BY s.link ORDER BY COUNT(s) DESC")
	Page<TopSong> findTopSkippedForUser(User u, Pageable pageable);

	@Cacheable(cacheNames = "stats", key = "'g-ts-'.concat(#u)",sync = true)
	@Query(value = "SELECT new de.elite12.musikbot.server.data.projection.TopSong(s.title, s.link, COUNT(s)) FROM Song s WHERE s.guestAuthor = ?1 AND s.skipped = true GROUP BY s.link ORDER BY COUNT(s) DESC")
	Page<TopSong> findTopSkippedForGuest(String u, Pageable pageable);
	
	@Query(value = "SELECT s FROM Song s WHERE s.userAuthor = ?1 ORDER BY s.sort DESC")
	Page<Song> findRecentByUser(User u, Pageable pageable);
	
	@Query(value = "SELECT s FROM Song s WHERE s.guestAuthor = ?1 ORDER BY s.sort DESC")
	Page<Song> findRecentByGuest(String u, Pageable pageable);
}
