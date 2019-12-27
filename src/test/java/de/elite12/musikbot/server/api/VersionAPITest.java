package de.elite12.musikbot.server.api;

import de.elite12.musikbot.server.config.MusikbotServiceProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Version.class)
public class VersionAPITest extends AbstractAPITest {

    private static String mockVersion = "CURRENT-VERSION";

    @MockBean
    protected MusikbotServiceProperties properties = null;

    @Before
    public void setUp() {
        Mockito.when(properties.getVersion()).thenReturn(mockVersion);
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
