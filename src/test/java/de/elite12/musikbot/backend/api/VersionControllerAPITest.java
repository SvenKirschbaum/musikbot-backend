package de.elite12.musikbot.backend.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = VersionController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
public class VersionControllerAPITest extends AbstractAPITest {

    private static final String mockVersion = "CURRENT-VERSION";

    @MockitoBean
    protected BuildProperties build = null;

    @BeforeEach
    public void setUp() {
        Mockito.when(build.getVersion()).thenReturn(mockVersion);
    }

    @Test
    public void getVersion() throws Exception {
        this.mockMvc
            .perform(get("/version"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("version").value(mockVersion));
    }
}
