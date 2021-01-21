package de.elite12.musikbot.server;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class MusikbotApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		try {
			SpringApplication.run(MusikbotApplication.class, args);
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApplicationRunner.class);
    }

}
