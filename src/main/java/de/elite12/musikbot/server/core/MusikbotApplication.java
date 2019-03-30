package de.elite12.musikbot.server.core;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("de.elite12.musikbot.server")
@EntityScan("de.elite12.musikbot.server.data.entity")
@EnableJpaRepositories("de.elite12.musikbot.server.data.repository")
public class MusikbotApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MusikbotApplication.class, args);
	}
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApplicationRunner.class);
    }

}
