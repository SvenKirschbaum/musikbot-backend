package de.elite12.musikbot.server.data.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.elite12.musikbot.server.data.entity.Token;
import de.elite12.musikbot.server.data.entity.User;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long>{
	public Optional<Token> findByToken(String token);
	
	public Optional<Token> findByOwner(User u);
}
