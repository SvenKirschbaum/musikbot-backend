package de.elite12.musikbot.server.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.elite12.musikbot.server.util.Util;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Locale;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @Column(unique = true)
    private String subject;
    @NotBlank
    @Column(unique = true)
    private String name;
    private boolean admin;
    @Email
    private String email;

    @Transient
    public String getGravatarId() {
        return Util.md5Hex(this.getEmail().toLowerCase(Locale.GERMAN));
    }
}
