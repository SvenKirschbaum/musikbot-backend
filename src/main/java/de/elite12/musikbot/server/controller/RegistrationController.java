package de.elite12.musikbot.server.controller;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.elite12.musikbot.server.data.UserMessage;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.services.MessageService;
import de.elite12.musikbot.server.services.UserDetailsService;
import de.elite12.musikbot.server.services.UserService;

@Controller
@RequestMapping("/register")
public class RegistrationController {
	
	@Autowired
	private MessageService messages;
	
	@Autowired
	private UserService userservice;
	
	@Autowired
	private UserDetailsService userdetailsservice;
	
	@Autowired
    private Validator validator;
	
	@Autowired
	private HttpServletRequest req;
	
	private static Logger logger = LoggerFactory.getLogger(RegistrationController.class);

	@GetMapping
	public String doGet() {
		if(!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
			return "redirect:/";
		}
		return "register";
	}
	
	@PostMapping
	public String doPost(@RequestParam("username") String username, @RequestParam("mail") String mail, @RequestParam("password") String password, @RequestParam("password2") String password2, @RequestParam(value = "datenschutz", required=false) boolean datenschutz) {
		if(!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
			return "redirect:/";
		}
	
		if(username.trim().isEmpty() || mail.trim().isEmpty() || password.trim().isEmpty() || password2.trim().isEmpty()) {
			messages.addMessage("Bitte fülle alle Felder aus!", UserMessage.TYPE_ERROR);
			return "register";
		}
		
		User ex = userservice.findUserbyName(username);
		if(ex != null) {
			messages.addMessage("Dieser Benutzername wird bereits verwendet!", UserMessage.TYPE_ERROR);
			return "register";
		}
		
		ex = userservice.findUserbyMail(mail);
		if(ex != null) {
			messages.addMessage("Diese Email wird bereits verwendet!", UserMessage.TYPE_ERROR);
			return "register";
		}
		
		if(password.trim().length() < 6) {
			messages.addMessage("Das Passwort muss aus mindestens 6 Zeichen bestehen", UserMessage.TYPE_ERROR);
			return "register";
		}
		
		if(!password.equals(password2)) {
			messages.addMessage("Die Passwörter stimmen nicht überein", UserMessage.TYPE_ERROR);
			return "register";
		}
		
		Email e = new Email();
    	e.v = mail;
        Set<ConstraintViolation<Email>> violations = validator.validate(e);
        
        if(!violations.isEmpty()) {
        	messages.addMessage("Die eingegebene Email-Adresse ist ungültig!", UserMessage.TYPE_ERROR);
			return "register";
        }
        
        if(!datenschutz) {
        	messages.addMessage("Du musst den Dazenschutzbestimmungen zustimmen", UserMessage.TYPE_ERROR);
			return "register";
        }
        
        User user = userservice.createUser(username, password, mail);
        UserDetails userdetails = userdetailsservice.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userdetails, null, userdetails.getAuthorities()));
        
        messages.addMessage("Registrierung erfolgreich", UserMessage.TYPE_SUCCESS);
        
        logger.info("Benutzer hat sich registriert: " + user + ", From IP: " + req.getRemoteAddr());
        
        
        
		return "redirect:/";
	}
	
	private static class Email {
    	@javax.validation.constraints.Email
    	String v;
    }
}
