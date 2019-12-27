package de.elite12.musikbot.server.api.v2;

import de.elite12.musikbot.server.api.dto.UsersDTO;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.UserRepository;
import de.elite12.musikbot.server.exception.BadRequestException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v2/users")
@RestController
@PreAuthorize("hasRole('admin')")
public class Users {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(Users.class);

    @GetMapping(value = {"", "{page}"})
    @ApiOperation(value = "Get the list of Users", notes = "Retrieves up to 25 Users. Use the page parameter to get more. Requires Admin Permissions.")
    public UsersDTO getUsers(@ApiParam(value = "The page to get") @PathVariable(name = "page", required = false) Integer opage) {
        int page = opage == null ? 1 : opage;

        if (page < 1 || page >= 85899347) throw new BadRequestException("The page parameter is not in the required range");

        Page<User> users = userRepository.findAll(PageRequest.of(page-1, 25));

        return new UsersDTO(users.getNumber()+1,users.getTotalPages(),users.get().toArray(User[]::new));
    }
}
