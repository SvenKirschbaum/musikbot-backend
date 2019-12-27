package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    @ApiModelProperty(notes = "The ID of the User")
    private Long id;
    @ApiModelProperty(notes = "The name of the User")
    private String name;
    @ApiModelProperty(notes = "If the User is a admin")
    private boolean admin;
    @ApiModelProperty(notes = "If the User is a guest")
    private boolean guest;
    @ApiModelProperty(notes = "The gravatar id for the user")
    private String gravatarId;
    @ApiModelProperty(notes = "How many Songs the User requested")
    private Long wuensche;
    @ApiModelProperty(notes = "How many Songs of the User have been skipped")
    private Long skipped;
    @ApiModelProperty(notes = "List of recently added Songs from the User")
    private GeneralEntry[] recent;
    @ApiModelProperty(notes = "List of the most wished Songs from the User")
    private TopEntry[] mostwished;
    @ApiModelProperty(notes = "List of the most skipped Songs added by the User")
    private TopEntry[] mostskipped;

    public void loadUser(User u) {
        this.setId(u.getId());
        this.setName(u.getName());
        this.setAdmin(u.isAdmin());
        this.setGravatarId(u.getGravatarId());
        this.setGuest(u.getEmail().equalsIgnoreCase("gast@elite12.de"));
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class AdminView extends UserDTO {
        @ApiModelProperty(notes = "Email of the User")
        private String email;

        @Override
        public void loadUser(User u) {
            super.loadUser(u);
            this.setEmail(u.getEmail());
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class GeneralEntry {
        private Long id;
        @ApiModelProperty(notes = "Title of the Song")
        private String title;
        @ApiModelProperty(notes = "URL of the Song")
        private String link;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class TopEntry {
        @ApiModelProperty(notes = "Title of the Song")
        private String title;
        @ApiModelProperty(notes = "Link of the Song")
        private String link;
        @ApiModelProperty(notes = "How often the Song has been requested/skipped")
        private Long count;
    }
}
