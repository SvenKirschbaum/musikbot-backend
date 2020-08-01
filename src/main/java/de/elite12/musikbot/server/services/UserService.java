package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.data.entity.Token;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.TokenRepository;
import de.elite12.musikbot.server.data.repository.UserRepository;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements PasswordEncoder {
    private final Argon2 argon2;

    @Autowired
    private UserRepository userrepository;

    @Autowired
    private TokenRepository tokenrepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService() {
        this.argon2 = Argon2Factory.create();
    }

    @PostConstruct
    public void postConstruct() {
        if(userrepository.count() == 0L) {
            User u = this.createUser("admin", "admin", "admin@example.com");
            u.setAdmin(true);
            this.saveUser(u);
            logger.info("There are no Users in the Database. A default Admin User has been created.");
        }
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
        return t.map(Token::getOwner).orElse(null);
    }

    public User saveUser(User u) {
        return userrepository.save(u);
    }

    @Transactional
    public User createUser(String username, String password, String email) {
        User u = new User();
        u.setName(username);
        if (password != null) u.setPassword(this.encode(password));
        else u.setPassword("null");
        u.setAdmin(false);
        u.setEmail(email);

        u = userrepository.save(u);
        return u;
    }

    public String getExternalToken(User u) {
        Optional<Token> t = tokenrepository.findByOwnerAndExternal(u, true);
        String r;
        if (t.isEmpty()) {
            r = resetExternalToken(u);
        } else {
            r = t.get().getToken();
        }
        return r;
    }

    public String resetExternalToken(User u) {
        Optional<Token> t = tokenrepository.findByOwnerAndExternal(u, true);

        t.ifPresent(tokenrepository::delete);

        Token token = new Token();
        token.setOwner(u);
        token.setCreated(new Date());
        token.setToken(UUID.randomUUID().toString());
        token.setExternal(true);
        token = tokenrepository.save(token);
        
        return token.getToken();
    }

    public boolean checkPassword(User user, String password) {
        boolean r =  this.matches(password, user.getPassword());
        if (r && user.getPassword().length() == 32) {
            user.setPassword(this.encode(password));
            this.saveUser(user);
        }
        return r;
    }

    public String getLoginToken(User user) {
        Token token = new Token();
        token.setOwner(user);
        token.setCreated(new Date());
        token.setToken(UUID.randomUUID().toString());
        token.setExternal(false);
        token = tokenrepository.save(token);

        return token.getToken();
    }

    @Scheduled(fixedRate = 3600000)
    //@Scheduled(fixedRate = 5000)
    protected void expireTokens() {
        tokenrepository.deleteExpiredTokens();
    }

    @Deprecated
    public String hashPW(String pw) {
        return this.argon2.hash(2, 65536, 1, pw);
    }

    private String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString(b & 0xFF | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            logger.error("Error calculating MD5", e);
        }
        return null;
    }
    
    @Override
    public String encode(CharSequence rawPassword) {
        return this.argon2.hash(2, 65536, 1, rawPassword.toString().toCharArray());
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword.length() == 32) {
            return encodedPassword.equals(this.MD5(rawPassword.toString()));
        } else {
            return this.argon2.verify(encodedPassword, rawPassword.toString().toCharArray());
        }
    }
}
