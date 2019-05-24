package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.LoginRequest;
import de.elite12.musikbot.server.api.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.UserService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v2/login")
public class Login {

    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    private final UserService userservice;

    @Autowired
    public Login(UserService userservice) {
        this.userservice = userservice;
    }

    @PostMapping
    public LoginResponse loginAction(@RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) {
        User u = userservice.findUserbyName(loginRequest.getUsername());
        if (u == null) {
            logger.info(String.format("Login failed (Wrong Username) by %s: %s", httpServletRequest.getRemoteAddr(), loginRequest.getUsername()));
            return new LoginResponse(false, "User not found", null);
        }
        if (userservice.checkPassword(u, loginRequest.getPassword())) {
            logger.info(String.format("Successful Login by %s",u.toString()));
            return new LoginResponse(true, "", userservice.getLoginToken(u));
        } else {
            logger.info(String.format("Login failed (Wrong Password) by %s: %s", httpServletRequest.getRemoteAddr(), loginRequest.getUsername()));
            return new LoginResponse(false, "Password wrong", null);
        }
    }

}
