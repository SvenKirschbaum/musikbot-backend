package de.elite12.musikbot.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private TaskScheduler messageBrokerTaskScheduler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue").setHeartbeatValue(new long[]{30000, 30000}).setTaskScheduler(this.messageBrokerTaskScheduler);
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/sock").setAllowedOriginPatterns("*").withSockJS().setSessionCookieNeeded(false).setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.4.0/sockjs.min.js");
        registry.addEndpoint("/client").setAllowedOriginPatterns("*").setHandshakeHandler(new DefaultHandshakeHandler() {
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
}
