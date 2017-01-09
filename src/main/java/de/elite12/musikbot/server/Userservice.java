package de.elite12.musikbot.server;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

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
		Logger.getLogger(this.getClass()).debug("Quarring User by ID");
		User u = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		Connection c = null;
		try {
			c = this.getControl().getDB();
			stmnt = c.prepareStatement("SELECT * FROM USER WHERE ID = ?");
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
			try {
				c.close();
			} catch (SQLException | NullPointerException e) {
				Logger.getLogger(this.getClass()).error("Error closing Connection", e);
			}
		}
		return u;
	}

	public User getUserbyName(String name) {
		Logger.getLogger(this.getClass()).debug("Quarring User by Name");
		User u = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		Connection c = null;
		try {
			c = this.getControl().getDB();
			stmnt = c.prepareStatement("SELECT * FROM USER WHERE Name = ?");
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
			try {
				c.close();
			} catch (SQLException | NullPointerException e) {
				Logger.getLogger(this.getClass()).error("Error closing Connection", e);
			}
		}
		return u;
	}

	public User getUserbyMail(String mail) {
		Logger.getLogger(this.getClass()).debug("Quarring User by ID");
		User u = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		Connection c = null;
		try {
			c = this.getControl().getDB();
			stmnt = c.prepareStatement("SELECT * FROM USER WHERE EMAIL = ?");
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
			try {
				c.close();
			} catch (SQLException | NullPointerException e) {
				Logger.getLogger(this.getClass()).error("Error closing Connection", e);
			}
		}
		return u;
	}

	public User getUserbyToken(String token) {
		Logger.getLogger(this.getClass()).debug("Quarring User by TOKEN");
		User u = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		Connection c = null;
		try {
			c = this.getControl().getDB();
			stmnt = c.prepareStatement("SELECT * FROM USER WHERE TOKEN = ?");
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
			try {
				c.close();
			} catch (SQLException | NullPointerException e) {
				Logger.getLogger(this.getClass()).error("Error closing Connection", e);
			}
		}
		return u;
	}

	public Integer changeUser(User u) {
		PreparedStatement statement = null;
		Connection c = null;
		Integer r = null;
		try {
			c = this.getControl().getDB();
			if (u.getId() != null) {
				statement = c.prepareStatement(
						"UPDATE USER SET NAME = ?, PASSWORD = ?, EMAIL = ?, ADMIN = ?, TOKEN = ? WHERE ID = ?");
				statement.setString(1, u.getName());
				statement.setString(2, u.getPassword());
				statement.setString(3, u.getEmail());
				statement.setBoolean(4, u.isAdmin());
				statement.setString(5, u.getToken());
				statement.setInt(6, u.getId());
				Logger.getLogger(this.getClass()).info("User changed: " + u);
			} else {
				Logger.getLogger(this.getClass()).error("Invalid call to changeUser: "+u);
				return 0;
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
			try {
				c.close();
			} catch (SQLException | NullPointerException e) {
				Logger.getLogger(this.getClass()).error("Error closing Connection", e);
			}
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

	public User createUser(String username, String password, String email) throws SQLException{
		User u = new User(username, password, email, false);
		PreparedStatement statement = null;
		Connection c = null;
		try {
			c = this.getControl().getDB();
			statement = c
					.prepareStatement("INSERT INTO USER (NAME, PASSWORD, EMAIL, ADMIN, TOKEN) VALUES (?, ?, ?, ?, ?)");
			statement.setString(1, u.getName());
			statement.setString(2, u.getPassword());
			statement.setString(3, u.getEmail());
			statement.setBoolean(4, u.isAdmin());
			statement.setString(5, u.getToken());
			Logger.getLogger(this.getClass()).info("User created: " + u);
		} catch (SQLException e) {
			Logger.getLogger(this.getClass()).error("Error changing User: " + u.getName(), e);
			throw e;
		} finally {
			try {
				statement.close();
			} catch (SQLException | NullPointerException e) {
				Logger.getLogger(this.getClass()).error("Error closing Statement", e);
			}
			try {
				c.close();
			} catch (SQLException | NullPointerException e) {
				Logger.getLogger(this.getClass()).error("Error closing Connection", e);
			}
		}
		return u;
	}

	public boolean checkPassword(User user, String password) {
		if(user.getPassword().length()==32) {
			if(user.getPassword().equals(this.MD5(password))) {
				user.setPassword(this.argon2.hash(2, 65536, 1, password));
				this.changeUser(user);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return this.argon2.verify(user.getPassword(), password);
		}
	}
}
