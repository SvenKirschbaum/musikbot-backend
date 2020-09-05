package de.elite12.musikbot.server.util;

import de.elite12.musikbot.server.services.JWTUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AuthenticationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);

    private final Cache authenticationCache;

    private final JWTUserService jwtUserService;

    public AuthenticationEventListener(CacheManager cacheManager, JWTUserService jwtUserService) {
        this.jwtUserService = jwtUserService;

        authenticationCache = cacheManager.getCache("oauth");
        Objects.requireNonNull(authenticationCache);
    }

    @EventListener
    public void handleAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
        Object credentials = event.getAuthentication().getCredentials();
        if (credentials instanceof Jwt) {
            Jwt jwt = (Jwt) credentials;
            if (authenticationCache.get(jwt.getClaim("jti")) == null) {
                jwtUserService.loadUserFromJWT(jwt, true);
                authenticationCache.put(jwt.getClaim("jti"), true);
            }
        }
    }

}
