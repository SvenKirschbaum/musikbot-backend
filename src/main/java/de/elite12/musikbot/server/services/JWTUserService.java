package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.config.MusikbotServiceProperties;
import de.elite12.musikbot.server.data.GuestSession;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JWTUserService {

    private final String resourceId;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final PushService pushService;

    @Autowired
    private GuestSession guestSession;

    public JWTUserService(MusikbotServiceProperties properties, UserRepository userRepository, SongRepository songRepository, PushService pushService) {
        this.resourceId = properties.getOauthResourceName();
        this.userRepository = userRepository;
        this.pushService = pushService;
        this.songRepository = songRepository;
    }


    public Collection<? extends GrantedAuthority> extractResourceRoles(Jwt source) {
        Map<String, Object> resourceAccess = source.getClaim("resource_access");

        if (resourceAccess != null) {
            Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(this.resourceId);
            if (resource != null) {
                Collection<String> resourceRoles = (Collection<String>) resource.get("roles");
                if (resourceRoles != null) {
                    return resourceRoles.stream()
                            .map(x -> new SimpleGrantedAuthority("ROLE_" + x))
                            .collect(Collectors.toSet());
                }
            }
        }

        return Collections.emptySet();
    }

    public User loadUserFromJWT(Jwt jwt) {
        return this.loadUserFromJWT(jwt, false);
    }

    public User loadUserFromJWT(Jwt jwt, boolean firstTime) {
        Optional<User> optionalUser = userRepository.findBySubject(jwt.getSubject());
        User user = optionalUser.orElseGet(User::new);

        user.setSubject(jwt.getSubject());
        user.setName(jwt.getClaimAsString("preferred_username"));
        user.setEmail(jwt.getClaimAsString("email"));

        Collection<? extends GrantedAuthority> authorities = this.extractResourceRoles(jwt);
        Boolean admin = authorities.stream().map((authority) -> authority.getAuthority().equals("ROLE_admin")).reduce(false, (b1, b2) -> b1 || b2);
        user.setAdmin(admin);

        userRepository.save(user);

        if (firstTime) this.registerGuestSongs(user);

        return user;
    }

    private void registerGuestSongs(User user) {
        List<Song> songs = songRepository.findByGuestAuthor(guestSession.getId());
        songs.forEach(song -> {
            song.setUserAuthor(user);
            song.setGuestAuthor(null);
        });
        songRepository.saveAll(songs);
        if (songs.size() > 0) pushService.sendState();
    }
}
