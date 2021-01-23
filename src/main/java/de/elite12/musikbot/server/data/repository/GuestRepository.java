package de.elite12.musikbot.server.data.repository;

import de.elite12.musikbot.server.data.entity.Guest;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestRepository extends PagingAndSortingRepository<Guest, Long> {
    Optional<Guest> findByToken(String token);

    Optional<Guest> findByIdentifier(String identifier);
}
