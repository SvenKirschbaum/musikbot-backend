package de.elite12.musikbot.server.core;

import java.util.Arrays;
import java.util.Collections;

import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import de.elite12.musikbot.server.filter.TokenFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableScheduling
public class MusikbotServiceConfig {
	
	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Autowired
		private TokenFilter tokenFilter;

		@Override
	    protected void configure(HttpSecurity http) throws Exception {
			http
				.csrf().disable()
				.cors().and()
				.sessionManagement().maximumSessions(1).and().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
				.headers()
					.xssProtection()
						.disable()
					.contentTypeOptions()
						.disable()
					.frameOptions()
						.disable()
					.and();
	    }
		
		@Bean
		public FilterRegistrationBean<TokenFilter> TokenFilterRegistration() {
		    FilterRegistrationBean<TokenFilter> registration = new FilterRegistrationBean<>(tokenFilter);
		    registration.setEnabled(false);
		    return registration;
		}

		@Bean
		public RemoteIpFilter remoteIpFilter() {
			return new RemoteIpFilter();
		}
		
		@Bean
		CorsConfigurationSource corsConfigurationSource() {
			CorsConfiguration configuration = new CorsConfiguration();
			configuration.setAllowedOrigins(Collections.singletonList("*"));
			configuration.setAllowedHeaders(Arrays.asList("origin", "content-type", "accept", "authorization"));
			configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", configuration);
			return source;
		}
	}
}
