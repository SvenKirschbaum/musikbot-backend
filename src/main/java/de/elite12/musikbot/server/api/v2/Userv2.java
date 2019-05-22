package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.TokenDTO;
import de.elite12.musikbot.server.api.dto.UserDTO;
import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.exception.NotFoundException;
import de.elite12.musikbot.server.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RequestMapping("/api/v2/user")
@RestController
public class Userv2 {

    @Autowired
    UserService userservice;

    @Autowired
    SongRepository songRepository;

    @Autowired
    private Validator validator;

    private static Logger logger = LoggerFactory.getLogger(Userv2.class);

    @GetMapping("{username}")
    public UserDTO getUserByName(@PathVariable("username") String username) {
        User target = userservice.findUserbyName(username);

        User user = null;
        boolean admin = false;
        boolean self = false;
        boolean guest = false;

        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof UserPrincipal) {
            UserPrincipal t = (UserPrincipal) p;
            user = t.getUser();
            admin = user.isAdmin();
        }

        if (target == null) {
            try {
                String name = UUID.fromString(username).toString();
                target = new User();
                target.setName(name);
                target.setEmail("gast@elite12.de");
                guest = true;
            } catch (IllegalArgumentException e) {
                throw new NotFoundException();
            }
        } else {
            if (user != null && user.getId().equals(target.getId())) {
                self = true;
            }
        }

        UserDTO r = null;

        if ((user != null && user.isAdmin()) || self) {
            r = new UserDTO.AdminView();
        } else {
            r = new UserDTO();
        }

        r.loadUser(target);

        r.setWuensche(
            (guest ?
                songRepository.countByGuestAuthor(username) :
                songRepository.countByUserAuthor(target)
            )
        );
        r.setSkipped(
            (guest ?
                songRepository.countByGuestAuthorAndSkipped(username, true) :
                songRepository.countByUserAuthorAndSkipped(target, true)
            )
        );


        r.setRecent(
                StreamSupport
                        .stream(
                                (guest ?
                                        songRepository.findRecentByGuest(username) :
                                        songRepository.findRecentByUser(target)
                                )
                                        .spliterator(),
                                false
                        )
                        .map(
                                tuple ->
                                        new UserDTO.GeneralEntry(
                                                tuple.get(0, BigInteger.class).longValue(),
                                                tuple.get(1, String.class),
                                                tuple.get(2, String.class)
                                        )
                        )
                        .toArray(UserDTO.GeneralEntry[]::new)
        );

        r.setMostwished(
                StreamSupport
                        .stream(
                                (guest ?
                                        songRepository.findTopByGuest(username) :
                                        songRepository.findTopByUser(target)
                                )
                                        .spliterator(),
                                false
                        )
                        .map(
                                tuple ->
                                        new UserDTO.TopEntry(
                                                tuple.get(0, String.class),
                                                tuple.get(1, String.class),
                                                tuple.get(2, BigInteger.class).longValue()
                                        )
                        )
                        .toArray(UserDTO.TopEntry[]::new)
        );

        r.setMostskipped(
                StreamSupport
                        .stream(
                                (guest ?
                                        songRepository.findTopSkippedByGuest(username) :
                                        songRepository.findTopSkippedByUser(target)
                                )
                                        .spliterator(),
                                false
                        )
                        .map(
                                tuple ->
                                        new UserDTO.TopEntry(
                                                tuple.get(0, String.class),
                                                tuple.get(1, String.class),
                                                tuple.get(2, BigInteger.class).longValue()
                                        )
                        )
                        .toArray(UserDTO.TopEntry[]::new)
        );

        return r;
    }

    @RequestMapping(path = "{userid}/{attribute}", method = RequestMethod.POST, consumes = {"text/plain"})
    public ResponseEntity<String> updateUser(@PathVariable("userid") Long userid, @PathVariable("attribute") String attr, @RequestBody String value) {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        de.elite12.musikbot.server.data.entity.User user = null;
        if (p instanceof UserPrincipal) {
            user = ((UserPrincipal) p).getUser();
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        de.elite12.musikbot.server.data.entity.User target = userservice.findUserbyId(userid);
        if (target == null) {
            return new ResponseEntity<>("Userid ungültig", HttpStatus.NOT_FOUND);
        }
        switch (attr) {
            case "email": {
                if (selforadmin(user, target)) {
                    Email e = new Email();
                    e.v = value;
                    Set<ConstraintViolation<Email>> violations = validator.validate(e);

                    if (violations.isEmpty()) {
                        target.setEmail(value);
                        userservice.saveUser(target);
                        logger.info(user + " changed Email-Address of " + target + "to " + target.getEmail());
                        return new ResponseEntity<>(HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(((ConstraintViolation<Email>) violations.toArray()[0]).getMessage(), HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            case "password": {
                if (selforadmin(user, target)) {
                    target.setPassword(userservice.encode(value));
                    userservice.saveUser(target);
                    logger.info(user + " changed the Password of " + target);
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            case "admin": {
                if (user.isAdmin()) {
                    boolean admin = value.equalsIgnoreCase("ja") || value.equalsIgnoreCase("yes")
                            || value.equalsIgnoreCase("true");
                    target.setAdmin(admin);
                    userservice.saveUser(target);
                    logger.info(user + " changed the Admin-Status of " + target + " to " + target.isAdmin());
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            case "username": {
                if (user.isAdmin()) {
                    if (userservice.findUserbyName(value) == null) {
                        logger.info(user + " changed the Username of " + target + " to " + value);
                        target.setName(value);
                        userservice.saveUser(target);
                        return new ResponseEntity<>(HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(HttpStatus.CONFLICT);
                    }
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            default: {
                return new ResponseEntity<>("Attribut ungültig", HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping(path = "self")
    @PreAuthorize("isAuthenticated()")
    public de.elite12.musikbot.server.data.entity.User getSelf() {
        de.elite12.musikbot.server.data.entity.User user = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        return user;
    }

    @GetMapping(path = "self/token")
    @PreAuthorize("isAuthenticated()")
    public TokenDTO getToken() {
        return new TokenDTO(userservice.getExternalToken(((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser()));
    }

    @PostMapping(path = "self/token/reset")
    @PreAuthorize("isAuthenticated()")
    public TokenDTO resetToken() {
        userservice.resetExternalToken(((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser());
        return new TokenDTO(userservice.getExternalToken(((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser()));
    }

    private static class Email {
        @javax.validation.constraints.Email
        String v;
    }

    private boolean selforadmin(de.elite12.musikbot.server.data.entity.User user, de.elite12.musikbot.server.data.entity.User target) {
        return user.equals(target) || user.isAdmin();
    }
}
