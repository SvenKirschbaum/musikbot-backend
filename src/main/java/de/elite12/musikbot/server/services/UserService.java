package de.elite12.musikbot.server.services;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.elite12.musikbot.server.data.entity.Token;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.TokenRepository;
import de.elite12.musikbot.server.data.repository.UserRepository;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

@Service
public class UserService {
    private Argon2 argon2;
    
    @Autowired
    private UserRepository userrepository;
    
    @Autowired
    private TokenRepository tokenrepository;
    
    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public UserService() {
        this.argon2 = Argon2Factory.create();
    }
    
    public User findUserbyId(Long id) {
        logger.debug("Querying User by ID");
        return userrepository.findById(id).orElse(null);
    }
    
    public User findUserbyName(String name) {
        logger.debug("Querying User by Name");
        return userrepository.findByName(name);
    }
    
    public User findUserbyMail(String mail) {
        logger.debug("Querying User by ID");
        return userrepository.findByEmail(mail);
    }
    
    public User findUserbyToken(String token) {
        logger.debug("Querying User by TOKEN");
        Optional<Token> t = tokenrepository.findByToken(token);
        return t.isPresent() ? t.get().getOwner() : null;
    }
    
    public User saveUser(User u) {
    	return userrepository.save(u);
    }
    
    public User createUser(String username, String password, String email) {
    	User u = new User();
    	u.setName(username);
    	u.setPassword(this.argon2.hash(2, 65536, 1, password));
    	u.setAdmin(false);
    	u.setEmail(email);
    	u = userrepository.save(u);
    	return u;
    }
    
    public String getExternalToken(User u) {
    	Optional<Token> t = tokenrepository.findByOwner(u);
    	String r = null;
    	if(!t.isPresent()) {
    		r = resetExternalToken(u);
    	}
    	else {
    		r = t.get().getToken();
    	}
    	return r;
    }
    
    public String resetExternalToken(User u) {
    	Optional<Token> t = tokenrepository.findByOwner(u);
    	if(t.isPresent()) {
    		tokenrepository.delete(t.get());
    	}
    	Token token = new Token();
    	token.setOwner(u);
    	token.setCreated(new Date());
    	token.setToken(UUID.randomUUID().toString());
    	token = tokenrepository.save(token);

    	return token.getToken();
    }
    
    public boolean checkPassword(User user, String password) {
        if (user.getPassword().length() == 32) {
            if (user.getPassword().equals(this.MD5(password))) {
                user.setPassword(this.argon2.hash(2, 65536, 1, password));
                this.saveUser(user);
                return true;
            } else {
                return false;
            }
        } else {
            return this.argon2.verify(user.getPassword(), password);
        }
    }
    
    public String hashPW(String pw) {
    	return this.argon2.hash(2, 65536, 1, pw);
    }
    
    private String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString(array[i] & 0xFF | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException | UnsupportedEncodingException e) {
            logger.error("Error calculating MD5", e);
        }
        return null;
    }
}
