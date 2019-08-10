package de.elite12.musikbot.server.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import de.elite12.musikbot.server.data.entity.Token;
import de.elite12.musikbot.server.data.entity.User;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    
    Optional<Token> findByOwnerAndExternal(User u, boolean external);

    @Query(value = "DELETE FROM token WHERE token.external = false AND token.created < NOW() - INTERVAL 7 DAY", nativeQuery = true)
    void deleteExpiredTokens();
}
