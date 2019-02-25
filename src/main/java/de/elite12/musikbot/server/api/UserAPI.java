package de.elite12.musikbot.server.api;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.UserService;

@RequestMapping("/api/user")
@RestController
public class UserAPI {
    
    @Autowired
    HttpServletRequest req;
    
    @Autowired
    UserService userservice;
    
    @Autowired
    private Validator validator;
    
    private static Logger logger = LoggerFactory.getLogger(UserAPI.class);
    
    @SuppressWarnings("unchecked")
	@RequestMapping(path="{userid}/{attribute}",method = RequestMethod.POST, consumes = {"text/plain"})
    public ResponseEntity<String> updateUser(@PathVariable("userid") Long userid, @PathVariable("attribute") String attr, @RequestBody String value) {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = null;
        if(p instanceof UserPrincipal) {
        	user = ((UserPrincipal)p).getUser();
        }
        else {
        	return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User target = userservice.findUserbyId(userid);
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
    
    private static class Email {
    	@javax.validation.constraints.Email
    	String v;
    }
    
    private boolean selforadmin(User user, User target) {
        return user.equals(target) || user.isAdmin();
    }
}
