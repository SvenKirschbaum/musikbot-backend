package de.elite12.musikbot.backend.api.dto;

import de.elite12.musikbot.backend.data.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    @Schema(description = "The ID of the User")
    private Long id;
    @Schema(description = "The name of the User")
    private String name;
    @Schema(description = "If the User is a admin")
    private boolean admin;
    @Schema(description = "If the User is a guest")
    private boolean guest;
    @Schema(description = "The gravatar id for the user")
    private String gravatarId;
    @Schema(description = "How many Songs the User requested")
    private Long wuensche;
    @Schema(description = "How many Songs of the User have been skipped")
    private Long skipped;
    @Schema(description = "List of recently added Songs from the User")
    private GeneralEntry[] recent;
    @Schema(description = "List of the most wished Songs from the User")
    private TopEntry[] mostwished;
    @Schema(description = "List of the most skipped Songs added by the User")
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
        @Schema(description = "Email of the User")
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
        @Schema(description = "Title of the Song")
        private String title;
        @Schema(description = "URL of the Song")
        private String link;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class TopEntry {
        @Schema(description = "Title of the Song")
        private String title;
        @Schema(description = "Link of the Song")
        private String link;
        @Schema(description = "How often the Song has been requested/skipped")
        private Long count;
    }
}
