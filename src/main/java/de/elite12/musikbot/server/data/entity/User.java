package de.elite12.musikbot.server.data.entity;

import java.io.Serializable;
import java.security.Principal;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import de.elite12.musikbot.server.util.Util;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class User implements Principal, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3629360425763011031L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
	@NotBlank
	@Size(max=64)
	private String name;
	private boolean admin;
	@ToString.Exclude
	private String password;
	@Email
	private String email;
	
	@Transient
	public String getGravatarId() {
		return Util.md5Hex(this.getEmail().toLowerCase(Locale.GERMAN));
	}
}
