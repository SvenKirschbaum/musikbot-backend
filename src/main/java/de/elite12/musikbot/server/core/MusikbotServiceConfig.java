package de.elite12.musikbot.server.core;

import java.util.Arrays;

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

import de.elite12.musikbot.server.filter.CookieFilter;
import de.elite12.musikbot.server.filter.TokenFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableScheduling
public class MusikbotServiceConfig {
	
	@Autowired
	private CookieFilter cookieFilter;
	
	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Autowired
	    private TokenFilter tokenFilter;
		
		@Override
	    protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**")
				.csrf().disable()
				.cors().and()
				.sessionManagement().maximumSessions(1).and().sessionCreationPolicy(SessionCreationPolicy.NEVER).and()
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
		CorsConfigurationSource corsConfigurationSource() {
			CorsConfiguration configuration = new CorsConfiguration();
			configuration.setAllowedOrigins(Arrays.asList("*"));
			configuration.setAllowedHeaders(Arrays.asList("origin", "content-type", "accept", "authorization"));
			configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", configuration);
			return source;
		}
	}
	
	@Configuration
	@Order(2)
	public static class MainWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		
		@Override
	    protected void configure(HttpSecurity http) throws Exception {
			http
				.sessionManagement()
					.maximumSessions(1)
					.and()
					.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
					.and()
				.formLogin()
					.loginPage("/")
					.loginProcessingUrl("/login")
					.defaultSuccessUrl("/")
					.usernameParameter("user")
					.passwordParameter("password")
					.failureUrl("/")
					.and()
				.logout()
					.logoutSuccessUrl("/")
					.and()
				.csrf()
					.and()
				.headers()
					.xssProtection()
						.disable()
					.contentTypeOptions()
						.disable()
					.frameOptions()
						.disable()
					.and();
	    }
	}
	
	@Bean
	public FilterRegistrationBean<CookieFilter> cookieFilterRegistration() {
		FilterRegistrationBean<CookieFilter> t = new FilterRegistrationBean<>();
		t.setFilter(cookieFilter);
		t.addUrlPatterns("/api/*");
		t.setMatchAfter(true);
		return t;
	}
}
