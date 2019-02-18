package de.elite12.musikbot.server.core;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.jdbc.support.SQLExceptionSubclassTranslator;

import de.elite12.musikbot.server.model.User;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Userservice {
    private Controller control;
    private Argon2 argon2;
    
    public Userservice(Controller con) {
        this.control = con;
        this.argon2 = Argon2Factory.create();
    }
    
    public User getUserbyId(int id) {
        Logger.getLogger(this.getClass()).debug("Querying User by ID");
        User u = null;
        try (
                Connection c = this.getControl().getDB();
                PreparedStatement stmnt = c.prepareStatement("SELECT u.ID,u.NAME,u.PASSWORD,u.EMAIL,u.ADMIN FROM USER u WHERE u.id = ?");
        ) {
            stmnt.setInt(1, id);
            ResultSet rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error querrying User", e);
        }
        return u;
    }
    
    public User getUserbyName(String name) {
        Logger.getLogger(this.getClass()).debug("Querying User by Name");
        User u = null;
        try (
                Connection c = this.getControl().getDB();
                PreparedStatement stmnt = c.prepareStatement("SELECT u.ID,u.NAME,u.PASSWORD,u.EMAIL,u.ADMIN FROM USER u WHERE u.NAME = ?");
        ) {
            stmnt.setString(1, name);
            ResultSet rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error querrying User", e);
        }
        return u;
    }
    
    public User getUserbyMail(String mail) {
        Logger.getLogger(this.getClass()).debug("Querying User by ID");
        User u = null;
        try (
                Connection c = this.getControl().getDB();
                PreparedStatement stmnt = c.prepareStatement("SELECT u.ID,u.NAME,u.PASSWORD,u.EMAIL,u.ADMIN FROM USER u WHERE u.EMAIL = ?");
        ) {
            stmnt.setString(1, mail);
            ResultSet rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error querrying User", e);
        }
        return u;
    }
    
    public User getUserbyToken(String token) {
        Logger.getLogger(this.getClass()).debug("Querying User by TOKEN");
        User u = null;
        try (
                Connection c = this.getControl().getDB();
                PreparedStatement stmnt = c.prepareStatement("SELECT u.ID,u.NAME,u.PASSWORD,u.EMAIL,u.ADMIN FROM AUTHTOKENS t1 INNER JOIN USER u ON t1.OWNER = u.ID WHERE t1.TOKEN = ?");
        ) {
            stmnt.setString(1, token);
            ResultSet rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error querrying User", e);
        }
        return u;
    }
    
    public Integer changeUser(User u) {
        Integer r = null;
        
        try (
        	Connection c = this.getControl().getDB();
    		PreparedStatement statement = c.prepareStatement(
                    "UPDATE USER SET NAME = ?, PASSWORD = ?, EMAIL = ?, ADMIN = ? WHERE ID = ?");
        ) {
            if (u.getId() != null) {
                
                statement.setString(1, u.getName());
                statement.setString(2, u.getPassword());
                statement.setString(3, u.getEmail());
                statement.setBoolean(4, u.isAdmin());
                statement.setInt(5, u.getId());
                
                Logger.getLogger(this.getClass()).info("User changed: " + u);
            } else {
                Logger.getLogger(this.getClass()).error("Invalid call to changeUser: " + u);
                return 0;
            }
            r = statement.executeUpdate();
        } catch (SQLException e) {
        	Logger.getLogger(this.getClass()).error("Error changing User: " + u.getName(),e);
        }
        return r;
    }
    
    private Controller getControl() {
        return this.control;
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
            Logger.getLogger(this.getClass()).error("Error calculating MD5", e);
        }
        return null;
    }
    
    public User createUser(String username, String password, String email) throws SQLException {
        User u = new User(username, this.argon2.hash(2, 65536, 1, password), email, false);
        try (
    		Connection c = this.getControl().getDB();
            PreparedStatement statement = c.prepareStatement(
                    "INSERT INTO USER (NAME, PASSWORD, EMAIL, ADMIN) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);	
        ) {
            statement.setString(1, u.getName());
            statement.setString(2, u.getPassword());
            statement.setString(3, u.getEmail());
            statement.setBoolean(4, u.isAdmin());
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if(rs.next()) {
            	u.setId(rs.getInt(1));
            }
            Logger.getLogger(this.getClass()).info("User created: " + u);
        } catch (SQLException e) {
			Logger.getLogger(this.getClass()).error("Error creating User: " + u.getName(),e);
        }
        return u;
    }
    
    public String getExternalToken(User u) {
    	try (
            Connection c = this.getControl().getDB();
            PreparedStatement stmnt = c.prepareStatement("SELECT t.TOKEN FROM AUTHTOKENS t WHERE t.TYPE = 'extern' AND t.OWNER = ?");
        ) {
            stmnt.setInt(1, u.getId());
            ResultSet rs = stmnt.executeQuery();
            if(rs.next()) {
            	return rs.getString("TOKEN");
            }
            else {
            	return resetExternalToken(u);
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error querrying User", e);
        }
    	return null;
    }
    
    public String resetExternalToken(User u) {
    	try (
            Connection c = this.getControl().getDB();
			PreparedStatement stmnt = c.prepareStatement("DELETE FROM AUTHTOKENS WHERE TYPE = 'extern' AND OWNER = ?");
			PreparedStatement stmnt2 = c.prepareStatement("INSERT INTO AUTHTOKENS (OWNER, TOKEN, TYPE) VALUES (?, ?, 'extern')");
        ) {
            stmnt.setInt(1, u.getId());
            stmnt.executeUpdate();
            
            String token = UUID.randomUUID().toString();
            
            stmnt2.setInt(1, u.getId());
            stmnt2.setString(2, token);
            stmnt2.executeUpdate();
            
            return token;
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error resetting Token", e);
        }
    	return null;
    }
    
    public String addToken(User u) {
    	try (
            Connection c = this.getControl().getDB();
			PreparedStatement stmnt = c.prepareStatement("INSERT INTO AUTHTOKENS (OWNER, TOKEN, TYPE) VALUES (?, ?, 'intern')");
        ) {
            String token = UUID.randomUUID().toString();
            
            stmnt.setInt(1, u.getId());
            stmnt.setString(2, token);
            stmnt.executeUpdate();
            
            return token;
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error adding Token", e);
        }
    	return null;
    }
    
    public void removeToken(String token) {
    	try (
            Connection c = this.getControl().getDB();
			PreparedStatement stmnt = c.prepareStatement("DELETE FROM AUTHTOKENS WHERE TOKEN = ?");
        ) {
            stmnt.setString(1, token);
            stmnt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error removing Token", e);
        }
    }
    
    public boolean checkPassword(User user, String password) {
        if (user.getPassword().length() == 32) {
            if (user.getPassword().equals(this.MD5(password))) {
                user.setPassword(this.argon2.hash(2, 65536, 1, password));
                this.changeUser(user);
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
}
