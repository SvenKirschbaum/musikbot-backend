package de.elite12.musikbot.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private TaskScheduler messageBrokerTaskScheduler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue").setHeartbeatValue(new long[]{30000, 30000}).setTaskScheduler(this.messageBrokerTaskScheduler);
        config.setApplicationDestinationPrefixes("/musikbot");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/sock").setAllowedOriginPatterns("*").withSockJS().setSessionCookieNeeded(false).setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.4.0/sockjs.min.js");
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
