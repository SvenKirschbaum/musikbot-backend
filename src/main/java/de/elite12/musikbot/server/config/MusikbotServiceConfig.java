package de.elite12.musikbot.server.config;

import de.elite12.musikbot.server.util.CustomJwtAuthenticationConverter;
import org.apache.catalina.filters.RemoteIpFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.stereotype.Controller;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

import java.security.Principal;
import java.util.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableScheduling
@EnableCaching
@EntityScan("de.elite12.musikbot.server.data.entity")
@EnableJpaRepositories("de.elite12.musikbot.server.data.repository")
public class MusikbotServiceConfig {

	@Configuration
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		@Autowired
		CustomJwtAuthenticationConverter jwtAuthenticationConverter;

		@Override
	    protected void configure(HttpSecurity http) throws Exception {
			http
					.csrf().disable()
					.cors().and()
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
					.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter).and().and()
					.headers()
					.xssProtection()
					.disable()
					.contentTypeOptions()
					.disable()
					.frameOptions()
					.disable()
					.and()
					.authorizeRequests().requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("actuator");
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
			configuration.setAllowCredentials(true);
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

		private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

		@Autowired
		private TaskScheduler messageBrokerTaskScheduler;

		@Override
		public void configureMessageBroker(MessageBrokerRegistry config) {
			config.enableSimpleBroker("/topic", "/queue").setHeartbeatValue(new long[]{30000, 30000}).setTaskScheduler(this.messageBrokerTaskScheduler);
			config.setApplicationDestinationPrefixes("/app");
		}

		@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			registry.addEndpoint("/sock").setAllowedOrigins("*").withSockJS().setSessionCookieNeeded(false).setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.4.0/sockjs.min.js");
			registry.addEndpoint("/client").setAllowedOrigins("*").setHandshakeHandler(new DefaultHandshakeHandler() {
				@Override
				protected Principal determineUser(@NonNull ServerHttpRequest request,
												  @NonNull WebSocketHandler wsHandler,
												  @NonNull Map<String, Object> attributes) {
					// Generate principal with UUID as name
					return new Principal() {
						private final String name = UUID.randomUUID().toString();

						@Override
						public String getName() {
							return this.name;
						}
					};
				}
			});
		}

		@Override
		public void configureClientInboundChannel(ChannelRegistration registration) {
			registration.interceptors(new ChannelInterceptor() {
				@Override
				public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
					StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
					if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
						final String header = accessor.getFirstNativeHeader("Authorization");
						//TODO: Handle websocket authentication
						//User u = null;
						//if(u != null) {
						//	UserPrincipal up = new UserPrincipal(u);
						//	accessor.setUser(new UsernamePasswordAuthenticationToken(up, "", up.getAuthorities()));
						//}
						//else if(header != null && !header.isEmpty()) {
						//	throw new BadCredentialsException("Invalid Token supplied");
						//}
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

	@Configuration
	@Import(BeanValidatorPluginsConfiguration.class)
	public static class Swagger2Config {
		@Autowired
		private MusikbotServiceProperties musikbotServiceProperties;

		@Bean
		public Docket api() {
			return new Docket(DocumentationType.SWAGGER_2)
					.select()
					.apis(RequestHandlerSelectors
							.basePackage("de.elite12.musikbot.server.api")
					)
					.build()
					.apiInfo(apiEndPointsInfo())
					.protocols(Set.of("https"))
					.produces(Set.of("application/json", "application/xml"))
					.useDefaultResponseMessages(true)
					.securitySchemes(Collections.singletonList(
							new OAuth("Elite12 Identity",
									Collections.emptyList(),
									Collections.singletonList(new ImplicitGrant(
											new LoginEndpoint("https://id.elite12.de/auth/realms/elite12/protocol/openid-connect/auth"),
											"tokenName"
									))
							)
					))
					.securityContexts(Collections.singletonList(
							SecurityContext.builder()
									.securityReferences(Collections.singletonList(new SecurityReference("Elite12 Identity", new AuthorizationScope[0])))
									.operationSelector(operationContext -> true)
									.build()
					));
		}

		private ApiInfo apiEndPointsInfo() {
			return new ApiInfoBuilder().title("Musikbot REST API")
					.description("REST API to interact with the Musikbot Service")
					.contact(new Contact("Sven Kirschbaum", "https://www.kirschbaum.me", "sven@kirschbaum.me"))
					.version(musikbotServiceProperties.getVersion())
					.build();
		}

		@Bean
		SecurityConfiguration security() {
			return SecurityConfigurationBuilder.builder()
					.clientId("musikbot-frontend")
					.build();
		}

		@Bean
		UiConfiguration uiConfig() {
			return UiConfigurationBuilder.builder()
					.deepLinking(true)
					.build();
		}
	}
}
