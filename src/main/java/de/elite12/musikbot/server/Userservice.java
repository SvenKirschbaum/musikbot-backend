package de.elite12.musikbot.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class Userservice {
    private Controller control;

    public Userservice(Controller con) {
        this.control = con;
    }

    public User getUserbyId(int id) {
        Logger.getLogger(this.getClass()).debug("Quarring User by ID");
        User u = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = this.getControl().getDB().prepareStatement("SELECT * FROM USER WHERE ID = ?");
            stmnt.setInt(1, id);
            rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"), rs.getString("TOKEN"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error Quarrying User", e);
        } finally {
            try {
                stmnt.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Statement", e);
            }
            try {
                rs.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Resultset", e);
            }
        }
        return u;
    }

    public User getUserbyName(String name) {
        Logger.getLogger(this.getClass()).debug("Quarring User by Name");
        User u = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = this.getControl().getDB().prepareStatement("SELECT * FROM USER WHERE Name = ?");
            stmnt.setString(1, name);
            rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                Logger.getLogger(this.getClass()).debug("Usertoken: " + rs.getString("TOKEN"));
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"), rs.getString("TOKEN"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error Quarrying User", e);
        } finally {
            try {
                stmnt.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Statement", e);
            }
            try {
                rs.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Resultset", e);
            }
        }
        return u;
    }

    public User getUserbyMail(String mail) {
        Logger.getLogger(this.getClass()).debug("Quarring User by ID");
        User u = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = this.getControl().getDB().prepareStatement("SELECT * FROM USER WHERE EMAIL = ?");
            stmnt.setString(1, mail);
            rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"), rs.getString("TOKEN"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error Quarrying User", e);
        } finally {
            try {
                stmnt.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Statement", e);
            }
            try {
                rs.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Resultset", e);
            }
        }
        return u;
    }

    public User getUserbyToken(String token) {
        Logger.getLogger(this.getClass()).debug("Quarring User by TOKEN");
        User u = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = this.getControl().getDB().prepareStatement("SELECT * FROM USER WHERE TOKEN = ?");
            stmnt.setString(1, token);
            rs = stmnt.executeQuery();
            if (!rs.next()) {
                u = null;
            } else {
                u = new User(rs.getInt("id"), rs.getString("NAME"), rs.getString("PASSWORD"), rs.getString("EMAIL"),
                        rs.getBoolean("admin"), rs.getString("TOKEN"));
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error Quarrying User", e);
        } finally {
            try {
                stmnt.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Statement", e);
            }
            try {
                rs.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Resultset", e);
            }
        }
        return u;
    }

    public Integer changeUser(User u) {
        PreparedStatement statement = null;
        Integer r = null;
        try {
            if (u.getId() != null) {
                statement = this.getControl().getDB().prepareStatement(
                        "UPDATE USER SET NAME = ?, PASSWORD = ?, EMAIL = ?, ADMIN = ?, TOKEN = ? WHERE ID = ?");
                statement.setString(1, u.getName());
                statement.setString(2, u.getPassword());
                statement.setString(3, u.getEmail());
                statement.setBoolean(4, u.isAdmin());
                statement.setString(5, u.getToken());
                statement.setInt(6, u.getId());
                Logger.getLogger(this.getClass()).info("User changed: " + u);
            } else {
                statement = this.getControl().getDB().prepareStatement(
                        "INSERT INTO USER (NAME, PASSWORD, EMAIL, ADMIN, TOKEN) VALUES (?, ?, ?, ?, ?)");
                statement.setString(1, u.getName());
                statement.setString(2, u.getPassword());
                statement.setString(3, u.getEmail());
                statement.setBoolean(4, u.isAdmin());
                statement.setString(5, u.getToken());
                Logger.getLogger(this.getClass()).info("User created: " + u);
            }
            r = statement.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(this.getClass()).error("Error changing User: " + u.getName(), e);
        } finally {
            try {
                statement.close();
            } catch (SQLException | NullPointerException e) {
                Logger.getLogger(this.getClass()).error("Error closing Statement", e);
            }
        }
        return r;
    }

    private Controller getControl() {
        return this.control;
    }
}
