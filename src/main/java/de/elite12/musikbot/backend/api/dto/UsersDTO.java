package de.elite12.musikbot.backend.api.dto;

import de.elite12.musikbot.backend.data.entity.User;
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
