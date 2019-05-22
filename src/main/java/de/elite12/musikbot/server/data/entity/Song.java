package de.elite12.musikbot.server.data.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.elite12.musikbot.server.util.Util;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Song implements Serializable {    
    /**
	 * 
	 */
	private static final long serialVersionUID = -8316963238408846120L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean played;
    @NotNull
    private String link;
    @ManyToOne
	@JoinColumn(name="USER_AUTHOR", nullable=true)
    @JsonIgnore
    @Nullable
    private User userAuthor;
    @Nullable
    @JsonIgnore
    private String guestAuthor;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date insertedAt;
    @NotNull
    private String title;
    private boolean skipped = false;
    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    private Date playedAt;
    @Generated(GenerationTime.INSERT)
    private Long sort;
    private int duration;
    
    @Transient
    public String getGravatarId() {
    	return this.getUserAuthor()==null ? Util.md5Hex("null") : Util.md5Hex(this.getUserAuthor().getEmail().toLowerCase(Locale.GERMAN));
    }
    
    @Transient
    public String getAuthorLink() {
    	return this.getUserAuthor()==null ? this.getGuestAuthor() : this.getUserAuthor().getName();
    }
    
    @Transient
    public String getAuthor() {
        return this.getUserAuthor()==null ? "Gast" : this.getUserAuthor().getName();
    }
}
