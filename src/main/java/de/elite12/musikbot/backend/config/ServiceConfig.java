package de.elite12.musikbot.backend.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
@EntityScan("de.elite12.musikbot.server.data.entity")
@EnableJpaRepositories("de.elite12.musikbot.backend.data.repository")
public class ServiceConfig {


}
