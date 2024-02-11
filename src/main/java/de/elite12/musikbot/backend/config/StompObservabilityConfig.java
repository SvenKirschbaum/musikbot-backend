package de.elite12.musikbot.backend.config;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

@Configuration
public class StompObservabilityConfig {

    @Bean
    public MeterBinder websocketMetrics(WebSocketHandler webSocketHandler) {
        SubProtocolWebSocketHandler subProtocolWebSocketHandler = (SubProtocolWebSocketHandler) webSocketHandler;
        StompSubProtocolHandler stompSubProtocolHandler = subProtocolWebSocketHandler.getProtocolHandlers().stream().filter(handler -> handler instanceof StompSubProtocolHandler).map(handler -> (StompSubProtocolHandler) handler).findFirst().orElse(null);

        assert stompSubProtocolHandler != null;

        return (registry) -> {
            FunctionCounter.builder("websocket.sessions.total", subProtocolWebSocketHandler, (wsh) -> wsh.getStats().getTotalSessions()).register(registry);
            Gauge.builder("websocket.sessions.current", subProtocolWebSocketHandler, (wsh) -> wsh.getStats().getWebSocketSessions()).tag("type", "websocket").register(registry);
            Gauge.builder("websocket.sessions.current", subProtocolWebSocketHandler, (wsh) -> wsh.getStats().getHttpPollingSessions()).tag("type", "http-polling").register(registry);
            Gauge.builder("websocket.sessions.current", subProtocolWebSocketHandler, (wsh) -> wsh.getStats().getHttpStreamingSessions()).tag("type", "http-streaming").register(registry);
            FunctionCounter.builder("websocket.sessions.error", subProtocolWebSocketHandler, (wsh) -> wsh.getStats().getLimitExceededSessions()).tag("type", "limit-exceeded").register(registry);
            FunctionCounter.builder("websocket.sessions.error", subProtocolWebSocketHandler, (wsh) -> wsh.getStats().getNoMessagesReceivedSessions()).tag("type", "no-messages-received").register(registry);
            FunctionCounter.builder("websocket.sessions.error", subProtocolWebSocketHandler, (wsh) -> wsh.getStats().getTransportErrorSessions()).tag("type", "transport-error").register(registry);

            FunctionCounter.builder("stomp.frames.processed", stompSubProtocolHandler, (ssph) -> ssph.getStats().getTotalConnect()).tag("type", "connect").register(registry);
            FunctionCounter.builder("stomp.frames.processed", stompSubProtocolHandler, (ssph) -> ssph.getStats().getTotalDisconnect()).tag("type", "disconnect").register(registry);
            FunctionCounter.builder("stomp.frames.processed", stompSubProtocolHandler, (ssph) -> ssph.getStats().getTotalConnected()).tag("type", "connected").register(registry);
        };
    }
}
