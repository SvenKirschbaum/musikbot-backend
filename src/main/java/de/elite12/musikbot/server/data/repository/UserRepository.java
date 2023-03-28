package de.elite12.musikbot.server.data.repository;

import de.elite12.musikbot.server.data.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long>, CrudRepository<User, Long> {

    Optional<User> findBySubject(String subject);

    User findByName(String name);

    User findByEmail(String email);

    @Cacheable(cacheNames = "stats", key = "'admins-'.concat(#admin)", sync = true)
    Long countByAdmin(boolean admin);
}
