package de.elite12.musikbot.server.config;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.filter.TokenFilter;
import de.elite12.musikbot.server.services.UserService;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.server.ServerHttpRequest;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
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
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
	@Order(1)
	public static class ActuatorWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Autowired
		private TokenFilter tokenFilter;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.csrf().disable()
				.cors().and()
				.sessionManagement().disable()
				.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
				.requestMatcher(EndpointRequest.toAnyEndpoint())
					.authorizeRequests()
						.anyRequest()
							.hasRole("admin")
				.and();

		}
	}


	@Configuration
	@Order(2)
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
		@Autowired
		private UserService userservice;

		@Autowired
		private TaskScheduler messageBrokerTaskScheduler;


		@Override
		public void configureMessageBroker(MessageBrokerRegistry config) {
			config.enableSimpleBroker("/topic","/queue").setHeartbeatValue(new long[]{30000,30000}).setTaskScheduler(this.messageBrokerTaskScheduler);
			config.setApplicationDestinationPrefixes("/app");
		}

		@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			registry.addEndpoint("/sock").setAllowedOrigins("*").withSockJS().setSessionCookieNeeded(false).setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.4.0/sockjs.min.js");
			registry.addEndpoint("/client").setAllowedOrigins("*").setHandshakeHandler(new DefaultHandshakeHandler() {
				@Override
				protected Principal determineUser(ServerHttpRequest request,
												  WebSocketHandler wsHandler,
												  Map<String, Object> attributes) {
					// Generate principal with UUID as name
					return new Principal() {
						private String name = UUID.randomUUID().toString();
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

	@Configuration
	@EnableSwagger2
	@Import(BeanValidatorPluginsConfiguration.class)
	public static class Swagger2Config {
		@Autowired
		private MusikbotServiceProperties musikbotServiceProperties;

		private List<ResponseMessage> defaultResponses = List.of(
				new ResponseMessageBuilder()
						.code(401)
						.message("An invalid Authorization Token has been provided in the Authorization Header")
						.build(),
				new ResponseMessageBuilder()
						.code(403)
						.message("The provided Authorization Token has no Permission to access this resource, or no Token has been provided")
						.build(),
				new ResponseMessageBuilder()
						.code(400)
						.message("The Request does not fullfill the syntactic requirements for this endpoint")
						.build()
		);

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
							.host("musikbot.elite12.de")
							.produces(Set.of("application/json","application/xml"))
							.useDefaultResponseMessages(false)
							.globalResponseMessage(RequestMethod.GET, this.defaultResponses)
							.globalResponseMessage(RequestMethod.POST, this.defaultResponses)
							.globalResponseMessage(RequestMethod.PUT, this.defaultResponses)
							.globalResponseMessage(RequestMethod.DELETE, this.defaultResponses)
							.securitySchemes(List.of(new ApiKey("Bearer Token","Authorization", "header")))
							.securityContexts(List.of(
								SecurityContext.builder()
									.securityReferences(List.of(
											new SecurityReference(
												"Bearer Token",
												new AuthorizationScope[]{
													new AuthorizationScope("global","Beschreibung")
												}
											)
									))
									.forPaths(PathSelectors.any())
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
		UiConfiguration uiConfig() {
			return UiConfigurationBuilder.builder()
				.deepLinking(true)
				.build();
		}
	}
}
