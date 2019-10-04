package de.elite12.musikbot.server.core;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.filter.TokenFilter;
import de.elite12.musikbot.server.services.UserService;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableScheduling
@EnableCaching
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

	@Configuration
	@EnableWebSocket
	@EnableWebSocketMessageBroker
	@Controller
	public static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
		@Autowired
		private UserService userservice;

		@Override
		public void configureMessageBroker(MessageBrokerRegistry config) {
			config.enableSimpleBroker("/topic","/queue");
			config.setApplicationDestinationPrefixes("/app");
		}

		@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			registry.addEndpoint("/sock").setAllowedOrigins("*").withSockJS();
		}

		@Override
		public void configureClientInboundChannel(ChannelRegistration registration) {
			registration.interceptors(new ChannelInterceptor() {
				@Override
				public Message<?> preSend(Message<?> message, MessageChannel channel) {
					StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
					if(StompCommand.CONNECT.equals(accessor.getCommand())) {
						final String header = accessor.getFirstNativeHeader("Authorization");
						User u = userservice.findUserbyToken(TokenFilter.parseHeader(header));
						if(u != null) {
							UserPrincipal up = new UserPrincipal(u);
							accessor.setUser(new UsernamePasswordAuthenticationToken(up, "", up.getAuthorities()));
						}
						else if(header != null && !header.isEmpty()) {
							throw new BadCredentialsException("Invalid Token supplied");
						}
					}
					return message;
				}
			});
		}
	}


	@Configuration
	public static class ThreadConfig {
		@Bean
		@Primary
		public TaskExecutor threadPoolTaskExecutor() {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setCorePoolSize(2);
			executor.setMaxPoolSize(8);
			executor.setThreadNamePrefix("default_task_executor_thread");
			executor.initialize();
			return executor;
		}
	}
}
