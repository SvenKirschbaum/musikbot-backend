package de.elite12.musikbot.server.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.elite12.musikbot.server.util.Util;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.util.Locale;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = null;
    @NotBlank
    @Size(max = 64)
    @NotBlank
    @Column(unique = true)
    private String name;
    private boolean admin;
    @ToString.Exclude
    @JsonIgnore
    private String password;
    @Email
    @Column(unique = true)
    private String email;
    
    @Transient
    public String getGravatarId() {
        return Util.md5Hex(this.getEmail().toLowerCase(Locale.GERMAN));
    }
}
