package de.elite12.musikbot.server.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.elite12.musikbot.server.util.Util;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

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
    @ApiModelProperty(notes = "The ID of the Song")
    private Long id;
    @ApiModelProperty(notes = "If the Song has already been played")
    private boolean played;
    @NotNull
    @ApiModelProperty(notes = "The Link of the Song")
    private String link;
    @ManyToOne
	@JoinColumn(name="USER_AUTHOR")
    @JsonIgnore
    @Nullable
    private User userAuthor;
    @Nullable
    @JsonIgnore
    private String guestAuthor;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @ApiModelProperty(notes = "The Time the Song has been added")
    private Date insertedAt;
    @NotNull
    @ApiModelProperty(notes = "The title of the Song")
    private String title;
    @ApiModelProperty(notes = "If the Song has been skipped")
    private boolean skipped = false;
    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    @ApiModelProperty(notes = "The Time the Song has been played")
    private Date playedAt;
    @Generated(GenerationTime.INSERT)
    @ApiModelProperty(notes = "The sort order of the song")
    private Long sort;
    @ApiModelProperty(notes = "The duration of the Song")
    private int duration;
    
    @Transient
    public String getGravatarId() {
    	return this.getUserAuthor()==null ? Util.md5Hex("null") : Util.md5Hex(this.getUserAuthor().getEmail().toLowerCase(Locale.GERMAN));
    }
    
    @Transient
    public String getAuthorLink() {
        return this.getUserAuthor() == null ? (this.getGuestAuthor() == null ? null : this.getGuestAuthor()) : this.getUserAuthor().getName();
    }
    
    @Transient
    public String getAuthor() {
        return this.getUserAuthor() == null ? (this.getGuestAuthor() == null ? "System" : "Gast") : this.getUserAuthor().getName();
    }

    @PostPersist
    private void postPersist() {
        this.sort = this.id;
    }
}
