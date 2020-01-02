package de.elite12.musikbot.server.data.repository;

import de.elite12.musikbot.server.data.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
	
	User findByName(String name);
	
	User findByEmail(String email);

	@Query("SELECT COUNT(u.id) FROM User u")
	Long countAll();
}
