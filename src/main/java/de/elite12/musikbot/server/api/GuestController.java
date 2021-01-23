package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.data.entity.Guest;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.GuestRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.exception.UnauthorizedException;
import de.elite12.musikbot.server.services.JWTUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/guest")
public class GuestController {

    private static final Logger logger = LoggerFactory.getLogger(GuestController.class);

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private JWTUserService jwtUserService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void updateGuestSongs(@RequestHeader(name = "X-Guest-Token") String guestHeader) {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        User user = jwtUserService.loadUserFromJWT((Jwt) credentials);

        Guest guest;
        if (!guestHeader.isEmpty()) {
            Optional<Guest> optionalGuest = this.guestRepository.findByToken(guestHeader);
            if (optionalGuest.isEmpty()) {
                throw new UnauthorizedException("Guest Token invalid");
            }
            guest = optionalGuest.get();
        } else {
            throw new UnauthorizedException("Guest Token is required");
        }

        List<Song> songs = songRepository.findByGuestAuthor(guest);
        songs.forEach(song -> {
            song.setUserAuthor(user);
            song.setGuestAuthor(null);
        });
        songRepository.saveAll(songs);
        if (songs.size() > 0) {
            logger.info(String.format("Rewrote Songs from Guest %s to %s", guest.getIdentifier(), user.getName()));
        }
    }
}
