package de.elite12.musikbot.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/login")
public class Login {

    @Autowired
    private UserService userservice;

    @PostMapping
    public LoginResponse loginAction(@RequestBody LoginRequest request) {
        User u = userservice.findUserbyName(request.getUsername());
        if (u == null) {
            return new LoginResponse(false, "User not found", null, null);
        }
        if (userservice.checkPassword(u, request.getPassword())) {
            return new LoginResponse(true, "", userservice.getLoginToken(u), u);
        } else {
            return new LoginResponse(false, "Password wrong", null, null);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class LoginResponse {
        private boolean success;
        private String error;
        private String token;
        private User user;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class LoginRequest {
        private String username;
        private String password;
    }
}
