package de.elite12.musikbot.server.api.dto;

import de.elite12.musikbot.server.data.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsersDTO {

    private int page;

    private int pages;

    private User[] list;
}
