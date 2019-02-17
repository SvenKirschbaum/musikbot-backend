package de.elite12.musikbot.server.model;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

public class User implements Serializable, Principal {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9127024300063529868L;

	private Integer id = null;
	private String name;
	private boolean admin;
	private String password;
	private String email;
	private String token;

	public void setToken(String token) {
		this.token = token;
	}

	public User(String name, String password, String mail, boolean admin) {
		this.name = name;
		this.password = password;
		this.email = mail;
		this.admin = admin;
		this.token = (UUID.randomUUID()).toString();
	}

	public User(Integer id, String name, String password, String mail,
			boolean admin,String token) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.email = mail;
		this.admin = admin;
		this.token = token;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getToken() {
		return token;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (admin ? 1231 : 1237);
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (admin != other.admin)
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		return true;
	}
}
