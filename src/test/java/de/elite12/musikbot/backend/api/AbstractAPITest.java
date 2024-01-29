package de.elite12.musikbot.backend.api;

import de.elite12.musikbot.backend.services.JWTUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
public abstract class AbstractAPITest {

    @Autowired
    protected WebApplicationContext context;

    protected MockMvc mockMvc;

    @MockBean
    protected JWTUserService jwtUserService = null;

    @BeforeEach
    public void abstract_setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }
}
