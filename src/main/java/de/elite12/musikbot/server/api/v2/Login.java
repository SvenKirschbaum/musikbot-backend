package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.LoginRequest;
import de.elite12.musikbot.server.api.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.UserService;

@RestController
@RequestMapping("/api/v2/login")
public class Login {

    @Autowired
    private UserService userservice;

    @PostMapping
    public LoginResponse loginAction(@RequestBody LoginRequest request) {
        User u = userservice.findUserbyName(request.getUsername());
        if (u == null) {
            return new LoginResponse(false, "User not found", null);
        }
        if (userservice.checkPassword(u, request.getPassword())) {
            return new LoginResponse(true, "", userservice.getLoginToken(u));
        } else {
            return new LoginResponse(false, "Password wrong", null);
        }
    }

}
