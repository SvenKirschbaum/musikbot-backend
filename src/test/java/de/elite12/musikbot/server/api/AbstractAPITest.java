package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.services.JWTUserService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringRunner.class)
public abstract class AbstractAPITest {

    @Autowired
    protected WebApplicationContext context;

    protected MockMvc mockMvc;

    @MockBean
    protected JWTUserService jwtUserService = null;

    @Before
    public void abstract_setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }
}
