package de.elite12.musikbot.server.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.elite12.musikbot.server.util.Util;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serial;
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
    @Serial
    private static final long serialVersionUID = -8316963238408846120L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "The ID of the Song")
    private Long id;
    @Schema(description = "If the Song has already been played")
    private boolean played;
    @NotNull
    @Schema(description = "The Link of the Song")
    private String link;
    @ManyToOne
    @JoinColumn(name = "USER_AUTHOR")
    @JsonIgnore
    @Nullable
    private User userAuthor;
    @Nullable
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "GUEST_AUTHOR")
    private Guest guestAuthor;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Schema(description = "The Time the Song has been added")
    private Date insertedAt;
    @NotNull
    @Schema(description = "The title of the Song")
    private String title;
    @Schema(description = "If the Song has been skipped")
    private boolean skipped = false;
    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    @Schema(description = "The Time the Song has been played")
    private Date playedAt;
    @Generated(GenerationTime.INSERT)
    @Schema(description = "The sort order of the song")
    private Long sort;
    @Schema(description = "The duration of the Song")
    private int duration;
    
    @Transient
    public String getGravatarId() {
    	return this.getUserAuthor()==null ? Util.md5Hex("null") : Util.md5Hex(this.getUserAuthor().getEmail().toLowerCase(Locale.GERMAN));
    }
    
    @Transient
    public String getAuthorLink() {
        return this.getUserAuthor() == null ? (this.getGuestAuthor() == null ? null : this.getGuestAuthor().getIdentifier()) : this.getUserAuthor().getName();
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
