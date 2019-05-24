package de.elite12.musikbot.server.data.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.elite12.musikbot.server.data.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	
	User findByName(String name);
	
	User findByEmail(String email);
}
