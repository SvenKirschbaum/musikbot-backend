package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.config.ServiceProperties;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JWTUserService {

    private static final Logger logger = LoggerFactory.getLogger(JWTUserService.class);

    private final String resourceId;
    private final UserRepository userRepository;

    public JWTUserService(ServiceProperties properties, UserRepository userRepository) {
        this.resourceId = properties.getOauthResourceName();
        this.userRepository = userRepository;
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
        Optional<User> optionalUser = userRepository.findBySubject(jwt.getSubject());
        User user = optionalUser.orElseGet(User::new);

        user.setSubject(jwt.getSubject());
        user.setName(jwt.getClaimAsString("preferred_username"));
        user.setEmail(jwt.getClaimAsString("email"));

        Collection<? extends GrantedAuthority> authorities = this.extractResourceRoles(jwt);
        Boolean admin = authorities.stream().map((authority) -> authority.getAuthority().equals("ROLE_admin")).reduce(false, (b1, b2) -> b1 || b2);
        user.setAdmin(admin);

        userRepository.save(user);

        return user;
    }
}
