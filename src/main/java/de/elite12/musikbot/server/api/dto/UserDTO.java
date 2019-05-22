package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private boolean admin;
    private boolean guest;
    private String gravatarId;
    private Long wuensche;
    private Long skipped;
    private GeneralEntry[] recent;
    private TopEntry[] mostwished;
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
        private String title;
        private String link;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class TopEntry {
        private String title;
        private String link;
        private Long count;
    }
}
