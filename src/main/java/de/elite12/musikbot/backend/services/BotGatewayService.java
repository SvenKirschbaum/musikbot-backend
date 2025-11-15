package de.elite12.musikbot.backend.services;

import de.elite12.musikbot.proto.BotCommand;
import de.elite12.musikbot.proto.BotEvent;
import de.elite12.musikbot.proto.BotGatewayGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class BotGatewayService extends BotGatewayGrpc.BotGatewayImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BotGatewayService.class);
    private final Set<BotSession> clients = new CopyOnWriteArraySet<>();
    @Autowired
    private ClientService clientService;

    @Override
    public StreamObserver<BotEvent> connect(StreamObserver<BotCommand> responseObserver) {
        BotSession session = new BotSession(responseObserver);

        clients.add(session);

        this.clientService.onConnect();

        return session;
    }

    public boolean isNotConnected() {
        return this.clients.isEmpty();
    }

    public int getConnectedClients() {
        return this.clients.size();
    }

    public void sendCommand(BotCommand command) {
        for (BotSession client : clients) {
            try {
                client.responseObserver.onNext(command);
            } catch (Exception e) {
                logger.error("Error sending command to bot client", e);
                client.responseObserver.onError(e);
                clients.remove(client);
                clientService.onDisconnect();
            }
        }
    }

    private class BotSession implements StreamObserver<BotEvent> {
        public final StreamObserver<BotCommand> responseObserver;

        public BotSession(StreamObserver<BotCommand> responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        public void onNext(BotEvent value) {
            switch (value.getEventCase()) {
                case NO_LISTENERS -> {
                    clientService.onNoListener();
                }
                case SONG_REQUEST -> {
                    clientService.onRequestSong();
                }
                case EVENT_NOT_SET -> {
                    logger.warn("Received unknown bot event type");
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            clients.remove(this);
            clientService.onDisconnect();
        }

        @Override
        public void onCompleted() {
            clients.remove(this);
            clientService.onDisconnect();
        }
    }
}
