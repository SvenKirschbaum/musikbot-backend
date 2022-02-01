package de.elite12.musikbot.server.config;

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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
                    //Allow everyone to subscribe to state topic
                    .simpSubscribeDestMatchers("/topic/state").permitAll()
                    .simpSubscribeDestMatchers("/musikbot/state").permitAll()
                    //Allow everyone to use search autocomplete
                    .simpSubscribeDestMatchers("/user/queue/search").permitAll()
                    .simpMessageDestMatchers("/musikbot/search").permitAll()

                    //Require client role for client topic and client messages
                    .simpSubscribeDestMatchers("/topic/client").hasRole("client")
                    .simpMessageDestMatchers("/musikbot/client").hasRole("client")

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
                            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(decode);
                            jwtAuthenticationToken.setAuthenticated(true);
                            accessor.setUser(jwtAuthenticationToken);
                        }
                    }
                    return message;
                }
            });
        }
    }
}