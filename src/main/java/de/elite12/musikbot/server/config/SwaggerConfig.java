package de.elite12.musikbot.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Autowired
    private ServiceProperties serviceProperties;

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Musikbot REST API")
                        .description("REST API to interact with the Musikbot Service")
                        .version(serviceProperties.getVersion())
                        .contact(new Contact().name("Sven Kirschbaum").url("https://www.kirschbaum.me").email("sven@kirschbaum.me")))
                ;
    }
}
