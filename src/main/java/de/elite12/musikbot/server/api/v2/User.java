package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.services.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@RequestMapping("/api/v2/user")
@RestController
public class User {
    
    @Autowired
    HttpServletRequest req;
    
    @Autowired
    UserService userservice;
    
    @Autowired
    private Validator validator;
    
    private static Logger logger = LoggerFactory.getLogger(User.class);
    
    @SuppressWarnings("unchecked")
	@RequestMapping(path="{userid}/{attribute}",method = RequestMethod.POST, consumes = {"text/plain"})
    public ResponseEntity<String> updateUser(@PathVariable("userid") Long userid, @PathVariable("attribute") String attr, @RequestBody String value) {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        de.elite12.musikbot.server.data.entity.User user = null;
        if(p instanceof UserPrincipal) {
        	user = ((UserPrincipal)p).getUser();
        }
        else {
        	return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        de.elite12.musikbot.server.data.entity.User target = userservice.findUserbyId(userid);
        if(target == null) {
        	return new ResponseEntity<>("Userid ungültig",HttpStatus.NOT_FOUND);
        }
        switch (attr) {
            case "email": {
                if (selforadmin(user, target)) {
                	Email e = new Email();
                	e.v = value;
                    Set<ConstraintViolation<Email>> violations = validator.validate(e);
                    
                    if(violations.isEmpty()) {
                    	target.setEmail(value);
                        userservice.saveUser(target);
                        logger.info(user + " changed Email-Address of " + target + "to " + target.getEmail());
                        return new ResponseEntity<>(HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(((ConstraintViolation<Email>)violations.toArray()[0]).getMessage(),HttpStatus.BAD_REQUEST);
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
                }
                else {
                	return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            default: {
            	return new ResponseEntity<>("Attribut ungültig",HttpStatus.BAD_REQUEST);
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

    @Getter
    @Setter
    @AllArgsConstructor
    private static class TokenDTO {
        private String token;
    }
    
    private static class Email {
    	@javax.validation.constraints.Email
    	String v;
    }
    
    private boolean selforadmin(de.elite12.musikbot.server.data.entity.User user, de.elite12.musikbot.server.data.entity.User target) {
        return user.equals(target) || user.isAdmin();
    }
}
