package de.elite12.musikbot.backend.config;

import de.elite12.musikbot.backend.util.CustomJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig {
    @Configuration
    public static class GeneralWebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {

        @Autowired
        private TaskScheduler messageBrokerTaskScheduler;

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic", "/queue").setHeartbeatValue(new long[]{25000, 25000}).setTaskScheduler(this.messageBrokerTaskScheduler);
            config.setApplicationDestinationPrefixes("/musikbot");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/sock").setAllowedOriginPatterns("*").withSockJS().setSessionCookieNeeded(false).setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.0/sockjs.min.js");
            registry.addEndpoint("/client").setAllowedOriginPatterns("*");
        }

        @Override
        protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
            messages
                    //Allow messages without destination
                    .nullDestMatcher().permitAll()

                    //Require client role for client topic and client messages
                    .simpSubscribeDestMatchers("/topic/client").hasRole("client")
                    .simpMessageDestMatchers("/musikbot/client").hasRole("client")

                    //Admin only topics
                    .simpSubscribeDestMatchers("/topic/gapcloser").hasRole("admin")

                    //Allow everyone
                    .simpSubscribeDestMatchers("/topic/*").permitAll()
                    .simpSubscribeDestMatchers("/user/queue/*").permitAll()
                    .simpSubscribeDestMatchers("/musikbot/*").permitAll()
                    .simpMessageDestMatchers("/musikbot/*").permitAll()

                    //Deny all other messages
                    .anyMessage().denyAll();
        }

        @Override
        protected boolean sameOriginDisabled() {
            return true;
        }
    }

    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE + 99)
    public static class WebSocketAuthenticationConfig implements WebSocketMessageBrokerConfigurer {

        @Qualifier("jwtDecoderByIssuerUri")
        @Autowired
        JwtDecoder jwtAuthenticationProvider;

        @Autowired
        CustomJwtAuthenticationConverter jwtAuthenticationConverter;

        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            registration.interceptors(new ChannelInterceptor() {
                @Override
                public Message<?> preSend(Message<?> message, MessageChannel channel) {
                    StompHeaderAccessor accessor =
                            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        if (accessor.containsNativeHeader("Authorization")) {
                            String[] split = accessor.getFirstNativeHeader("Authorization").split(" ");
                            Jwt decode = jwtAuthenticationProvider.decode(split[split.length - 1]);
                            accessor.setUser(jwtAuthenticationConverter.convert(decode));
                        }
                    }
                    return message;
                }
            });
        }
    }
}
