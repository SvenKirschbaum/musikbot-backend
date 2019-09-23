package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.TokenDTO;
import de.elite12.musikbot.server.api.dto.UserDTO;
import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.Token;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.data.repository.TokenRepository;
import de.elite12.musikbot.server.data.repository.UserRepository;
import de.elite12.musikbot.server.exception.NotFoundException;
import de.elite12.musikbot.server.filter.TokenFilter;
import de.elite12.musikbot.server.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RequestMapping("/v2/user")
@RestController
public class Userv2 {

    @Autowired
    private UserService userservice;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private Validator validator;

    private static final Logger logger = LoggerFactory.getLogger(Userv2.class);

    @GetMapping("{username}")
    public UserDTO getUserByName(@PathVariable("username") String username) {
        User target = userservice.findUserbyName(username);

        User user = null;
        boolean self = false;
        boolean guest = false;

        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof UserPrincipal) {
            UserPrincipal t = (UserPrincipal) p;
            user = t.getUser();
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

        UserDTO r;

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
                                                tuple.getid(),
                                                tuple.gettitle(),
                                                tuple.getlink()
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
                                                tuple.gettitle(),
                                                tuple.getlink(),
                                                tuple.getcount()
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
                                                tuple.gettitle(),
                                                tuple.getlink(),
                                                tuple.getcount()
                                        )
                        )
                        .toArray(UserDTO.TopEntry[]::new)
        );

        return r;
    }

    @RequestMapping(path = "{userid}/{attribute}", method = RequestMethod.POST, consumes = {"text/plain"})
    public ResponseEntity<String> updateUser(@PathVariable("userid") Long userid, @PathVariable("attribute") String attr, @RequestBody String value) {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        de.elite12.musikbot.server.data.entity.User user;
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
                        logger.info(String.format("User changed by %s (Email): %s", user, target));
                        return new ResponseEntity<>(HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(violations.iterator().next().getMessage(), HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            case "password": {
                if (selforadmin(user, target)) {
                    target.setPassword(userservice.encode(value));
                    userservice.saveUser(target);
                    logger.info(String.format("User changed by %s (Password): %s", user, target));
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
                    logger.info(String.format("User changed by %s (Admin): %s", user, target));
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            case "username": {
                if (user.isAdmin()) {
                    if (userservice.findUserbyName(value) == null) {
                        String oldname = target.getName();
                        target.setName(value);
                        userservice.saveUser(target);
                        logger.info(String.format("User changed by %s (Name): %s -> %s", user, oldname, target));
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

    @DeleteMapping(path = "{userid}")
    @PreAuthorize("hasRole('admin')")
    @Transactional
    public ResponseEntity<Object> deleteUser(@PathVariable("userid") Long userid) {
        User user = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        Optional<User> target = userRepository.findById(userid);

        if(!target.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String guestid = UUID.randomUUID().toString();
        songRepository.replaceUserAuthor(target.get(), guestid);

        userRepository.delete(target.get());
        logger.info(String.format("User deleted by %s: %s -> %s", user.toString(), target.get().toString(), guestid));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "self")
    @PreAuthorize("isAuthenticated()")
    public de.elite12.musikbot.server.data.entity.User getSelf(HttpServletRequest httpServletRequest) {
        //Refresh token duration
        String authheader = httpServletRequest.getHeader("Authorization");
        String tokenstring = TokenFilter.parseHeader(authheader);
        Optional<Token> otoken = tokenRepository.findByToken(tokenstring);
        if(otoken.isPresent()) {
            Token token = otoken.get();
            if(!token.isExternal()) {
                token.setCreated(new Date());
                tokenRepository.save(token);
            }
        }
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
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
        logger.info(String.format("Token reset by %s", ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser().toString()));
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
