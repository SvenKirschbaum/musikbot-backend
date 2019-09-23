package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.LoginRequest;
import de.elite12.musikbot.server.api.dto.LoginResponse;
import de.elite12.musikbot.server.api.dto.RegistrationRequest;
import de.elite12.musikbot.server.data.GuestSession;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.entity.Token;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.data.repository.TokenRepository;
import de.elite12.musikbot.server.filter.TokenFilter;
import de.elite12.musikbot.server.services.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.UserService;

import javax.servlet.Registration;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class Login {

    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    @Autowired
    private UserService userservice;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private GuestSession guestinfo;

    @Autowired
    private PushService pushService;

    @Autowired
    private SongRepository songRepository;

    @PostMapping
    @RequestMapping("/v2/login")
    public LoginResponse loginAction(@RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) {
        User u = userservice.findUserbyName(loginRequest.getUsername());
        if (u == null) {
            logger.info(String.format("Login failed (Wrong Username) by %s: %s", httpServletRequest.getRemoteAddr(), loginRequest.getUsername()));
            return new LoginResponse(false, "User not found", null);
        }
        if (userservice.checkPassword(u, loginRequest.getPassword())) { //Success
            List<Song> songs = songRepository.findByGuestAuthor(guestinfo.getId());
            songs.forEach(song -> {
                song.setUserAuthor(u);
                song.setGuestAuthor(null);
            });
            songRepository.saveAll(songs);
            if(songs.size() > 0) pushService.sendState();
            logger.info(String.format("Successful Login by %s: %s", httpServletRequest.getRemoteAddr(), u.toString()));
            return new LoginResponse(true, "", userservice.getLoginToken(u));
        } else {
            logger.info(String.format("Login failed (Wrong Password) by %s: %s", httpServletRequest.getRemoteAddr(), loginRequest.getUsername()));
            return new LoginResponse(false, "Password wrong", null);
        }
    }

    @PostMapping
    @RequestMapping("/v2/logout")
    @PreAuthorize("isAuthenticated()")
    public LoginResponse logoutAction(HttpServletRequest httpServletRequest) {
        String authheader = httpServletRequest.getHeader("Authorization");
        String tokenstring = TokenFilter.parseHeader(authheader);
        Optional<Token> token = tokenRepository.findByToken(tokenstring);
        if(!token.orElseThrow().isExternal()) {
            tokenRepository.delete(token.orElseThrow());
            return new LoginResponse(true, "", null);
        }
        else {
            return new LoginResponse(false, "Provided Token is external", null);
        }
    }

    @PostMapping
    @RequestMapping("/v2/register")
    @PreAuthorize("isAnonymous()")
    public LoginResponse registerAction(@RequestBody @Valid RegistrationRequest data, BindingResult bindingResult) {
        User u = userservice.findUserbyName(data.getUsername());
        if(u != null) {
            return new LoginResponse(false,"Username already in use", null);
        }

        User e = userservice.findUserbyMail(data.getEmail());
        if(e != null) {
            return new LoginResponse(false,"Email already in use", null);
        }

        if(bindingResult.hasErrors()) {
            StringBuilder message = new StringBuilder();
            for (Object object : bindingResult.getAllErrors()) {
                if(object instanceof FieldError) {
                    FieldError fieldError = (FieldError) object;

                    message.append(fieldError.getField());
                    message.append(" - ");
                    message.append(fieldError.getDefaultMessage());
                    message.append("\n");
                }
                else if(object instanceof ObjectError) {
                    ObjectError objectError = (ObjectError) object;

                    message.append(objectError.getObjectName());
                    message.append(" - ");
                    message.append(objectError.getDefaultMessage());
                    message.append("\n");
                }
            }
            return new LoginResponse(false,message.toString(),null);
        }

        User user = userservice.createUser(data.getUsername(),data.getPassword(),data.getEmail());
        return new LoginResponse(true, "", userservice.getLoginToken(user));
    }

}
