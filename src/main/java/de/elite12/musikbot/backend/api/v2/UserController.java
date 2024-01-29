package de.elite12.musikbot.backend.api.v2;

import de.elite12.musikbot.backend.api.dto.UserDTO;
import de.elite12.musikbot.backend.data.entity.Guest;
import de.elite12.musikbot.backend.data.repository.GuestRepository;
import de.elite12.musikbot.backend.data.repository.SongRepository;
import de.elite12.musikbot.backend.data.repository.UserRepository;
import de.elite12.musikbot.backend.exceptions.api.NotFoundException;
import de.elite12.musikbot.backend.services.JWTUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RequestMapping("/v2/user/{username}")
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private JWTUserService jwtUserService;

    @Autowired
    private Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    @Operation(description = "Get a User Profile")
    public UserDTO getUserByName(@Parameter(description = "The Username of the User to get") @PathVariable("username") String username) {
        de.elite12.musikbot.backend.data.entity.User target = userRepository.findByName(username);
        Optional<Guest> guestTarget = guestRepository.findByIdentifier(username);

        de.elite12.musikbot.backend.data.entity.User user = null;
        boolean self = false;
        boolean guest = false;

        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();

        if (credentials instanceof Jwt) {
            user = jwtUserService.loadUserFromJWT((Jwt) credentials);
        }

        if (target == null) {
            try {
                String name = UUID.fromString(username).toString();
                target = new de.elite12.musikbot.backend.data.entity.User();
                target.setName(name);
                target.setEmail("gast@elite12.de");
                guest = true;
                if (guestTarget.isEmpty()) throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new NotFoundException();
            }
        } else {
            if (user != null && user.getSubject().equals(target.getSubject())) {
                self = true;
            }
        }

        UserDTO r;

        if ((user != null && user.isAdmin()) || self) {
            r = new UserDTO.AdminView();
        } else {
            r = new UserDTO();
        }

        r.loadUser(target);

        r.setWuensche(
            (guest ?
                    songRepository.countByGuestAuthor(guestTarget.get()) :
                    songRepository.countByUserAuthor(target)
            )
        );
        r.setSkipped(
            (guest ?
                    songRepository.countByGuestAuthorAndSkipped(guestTarget.get(), true) :
                    songRepository.countByUserAuthorAndSkipped(target, true)
            )
        );


        r.setRecent(
                StreamSupport
                        .stream(
                                (guest ?
                                        songRepository.findRecentByGuest(guestTarget.get(), PageRequest.of(0, 10)) :
                                        songRepository.findRecentByUser(target, PageRequest.of(0, 10))
                                )
                                        .spliterator(),
                                false
                        )
                        .map(
                                tuple ->
                                        new UserDTO.GeneralEntry(
                                                tuple.getId(),
                                                tuple.getTitle(),
                                                tuple.getLink()
                                        )
                        )
                        .toArray(UserDTO.GeneralEntry[]::new)
        );

        r.setMostwished(
                StreamSupport
                        .stream(
                                (guest ?
                                        songRepository.findTopForGuest(guestTarget.get(), PageRequest.of(0, 10)) :
                                        songRepository.findTopForUser(target, PageRequest.of(0, 10))
                                )
                                        .spliterator(),
                                false
                        )
                        .map(
                                tuple ->
                                        new UserDTO.TopEntry(
                                                tuple.getTitle(),
                                                tuple.getLink(),
                                                tuple.getCount()
                                        )
                        )
                        .toArray(UserDTO.TopEntry[]::new)
        );

        r.setMostskipped(
                StreamSupport
                        .stream(
                                (guest ?
                                        songRepository.findTopSkippedForGuest(guestTarget.get(), PageRequest.of(0, 10)) :
                                        songRepository.findTopSkippedForUser(target, PageRequest.of(0, 10))
                                )
                                        .spliterator(),
                                false
                        )
                        .map(
                                tuple ->
                                        new UserDTO.TopEntry(
                                                tuple.getTitle(),
                                                tuple.getLink(),
                                                tuple.getCount()
                                        )
                        )
                        .toArray(UserDTO.TopEntry[]::new)
        );

        return r;
    }

    @GetMapping(path = "/played")
    @Operation(description = "Get a Users most wished songs")
    public UserDTO.TopEntry[] getMostPlayedAction(@Parameter(description = "The Username of the User to get") @PathVariable("username") String username) {
        de.elite12.musikbot.backend.data.entity.User target = userRepository.findByName(username);
        Optional<Guest> guestTarget = guestRepository.findByIdentifier(username);
        boolean guest = false;

        if (target == null) {
            try {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString(username);
                guest = true;
                if (guestTarget.isEmpty()) throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new NotFoundException();
            }
        }

        return StreamSupport
                .stream(
                        (guest ?
                                songRepository.findTopForGuest(guestTarget.get(), PageRequest.of(0, 100)) :
                                songRepository.findTopForUser(target, PageRequest.of(0, 100))
                        )
                                .spliterator(),
                        false
                )
                .map(
                        tuple ->
                                new UserDTO.TopEntry(
                                        tuple.getTitle(),
                                        tuple.getLink(),
                                        tuple.getCount()
                                )
                )
                .toArray(UserDTO.TopEntry[]::new);
    }

    @GetMapping(path = "/skipped")
    @Operation(description = "Get a Users most skipped songs")
    public UserDTO.TopEntry[] getMostSkippedAction(@Parameter(description = "The Username of the User to get") @PathVariable("username") String username) {
        de.elite12.musikbot.backend.data.entity.User target = userRepository.findByName(username);
        Optional<Guest> guestTarget = guestRepository.findByIdentifier(username);
        boolean guest = false;

        if (target == null) {
            try {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString(username);
                guest = true;
                if (guestTarget.isEmpty()) throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new NotFoundException();
            }
        }

        return StreamSupport
                .stream(
                        (guest ?
                                songRepository.findTopSkippedForGuest(guestTarget.get(), PageRequest.of(0, 100)) :
                                songRepository.findTopSkippedForUser(target, PageRequest.of(0, 100))
                        )
                                .spliterator(),
                        false
                )
                .map(
                        tuple ->
                                new UserDTO.TopEntry(
                                        tuple.getTitle(),
                                        tuple.getLink(),
                                        tuple.getCount()
                                )
                )
                .toArray(UserDTO.TopEntry[]::new);
    }
}
