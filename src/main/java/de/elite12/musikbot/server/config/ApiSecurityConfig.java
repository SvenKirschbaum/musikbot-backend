package de.elite12.musikbot.server.config;

import de.elite12.musikbot.server.util.CustomJwtAuthenticationConverter;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class ApiSecurityConfig {

    @Autowired
    CustomJwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter).and().and()
                .headers()
                .xssProtection().disable()
                .contentTypeOptions().disable()
                .frameOptions().disable()
                .and()
                .authorizeHttpRequests()
                .anyRequest().permitAll();
        return http.build();
    }

    @Bean
    public RemoteIpFilter remoteIpFilter() {
        return new RemoteIpFilter();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Arrays.asList("origin", "content-type", "accept", "authorization"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
