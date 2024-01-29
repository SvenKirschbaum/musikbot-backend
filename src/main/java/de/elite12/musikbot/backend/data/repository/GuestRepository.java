package de.elite12.musikbot.backend.data.repository;

import de.elite12.musikbot.backend.data.entity.Guest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestRepository extends PagingAndSortingRepository<Guest, Long>, CrudRepository<Guest, Long> {
    Optional<Guest> findByToken(String token);

    Optional<Guest> findByIdentifier(String identifier);
}
