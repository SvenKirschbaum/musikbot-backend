package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.core.MusikbotServiceProperties;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.shared.clientDTO.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@Controller
public class ClientService {

	@Autowired
	private SongService songservice;

	@Autowired
	private MusikbotServiceProperties config;

	@Autowired
	private PushService pushService;

	@Autowired
	private SimpMessagingTemplate template;

	private Set<String> authorizedClients = new CopyOnWriteArraySet<>();

    private boolean waitforsong = false;
    private boolean ispaused = false;
    private State state = State.STARTED;
	
	private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

	public void notifynewSong() {
        logger.debug("Notify Song");
        if(isNotConnected()) return;
        if (this.waitforsong) {
            this.sendSong(songservice.getnextSong());
            this.waitforsong = false;
        }
    }
	
	public void pause() {
        logger.debug("Pausing");
        if(isNotConnected()) return;
        if (this.state == State.STARTED) {
            this.ispaused = !this.ispaused;

			if (this.ispaused) {
				songservice.setState("Paused");
			} else {
				songservice.setState("Playing");
			}

			this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.PAUSE));

			this.pushService.sendState();
        }
    }

    public void stop() {
        logger.debug("Stopping");
        if(isNotConnected()) return;
        if (this.state == State.STARTED) {
            this.state = State.STOPPED;

			songservice.setState("Stopped");
			songservice.setSongtitle("Kein Song");
			songservice.setSonglink(null);

			this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.STOP));

			this.pushService.sendState();
        }
    }

    public void start() {
        logger.debug("Starting...");
        if(isNotConnected()) return;
        if (this.state == State.STOPPED) {
            this.state = State.STARTED;
			onRequestSong();
        }
    }

    public void sendVolume(short volume) {
		logger.debug("Sending Volume...");
		if(isNotConnected()) return;
		this.sendCommand(new VolumeCommand(volume));
		this.songservice.setVolume(volume);
		this.pushService.sendState();
	}
    
    private void sendSong(Song song) {
        logger.debug("Sending Song...");
        if(isNotConnected()) return;

		this.waitforsong = false;
		songservice.setState("Playing");
		songservice.setSongtitle(song.getTitle());
		songservice.setSonglink(song.getLink());

		this.sendCommand(new de.elite12.musikbot.shared.clientDTO.Song(song.getLink(),song.getTitle(), song.getLink().contains("spotify") ? "spotify" : "youtube"));

		this.pushService.sendState();
    }

    public void sendShutdown() {
        logger.debug("Sending Shutdown...");
        if(isNotConnected()) return;
        this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.SHUTDOWN));
    }

	@MessageMapping("/client/song")
	private void onRequestSong() {
		Song song = songservice.getnextSong();
		if (song != null) {
			this.sendSong(song);
		} else {
			this.sendReply(new SimpleResponse(SimpleResponse.ResponseType.NO_SONG_AVAILABLE));
			songservice.setState("Warte auf neue Lieder");
			songservice.setSongtitle(null);
			songservice.setSonglink(null);
			this.waitforsong = true;
			this.pushService.sendState();
		}
	}

	@MessageMapping("/client/auth")
    private void onAuthRequest(Principal principal, Message<AuthRequest> message) {
		if(principal == null) return; //Client didn´t connect via correct endpoint

		if(message.getPayload().getKey().equals(config.getClientkey())) {
			if(this.authorizedClients.isEmpty()) {
				songservice.setState("Verbunden");
				this.pushService.sendState();
			}
			this.authorizedClients.add(principal.getName());
			this.template.convertAndSendToUser(principal.getName(),"/queue/reply", new AuthResponse(true), Map.of("type", AuthResponse.class.getSimpleName()));
			return;
		}
		this.template.convertAndSendToUser(principal.getName(),"/queue/reply", new AuthResponse(false), Map.of("type", AuthResponse.class.getSimpleName()));
	}

	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) {
		if(event.getUser() != null) {
			if(this.authorizedClients.contains(event.getUser().getName())) {
				this.authorizedClients.remove(event.getUser().getName());
				if(this.isNotConnected()) {
					songservice.setState("Keine Verbindung zum BOT");
					this.pushService.sendState();
				}
			}
		}
	}
    
    private boolean isNotConnected() {
    	return this.authorizedClients.isEmpty();
    }

    private void sendCommand(ClientDTO clientDTO) {
		for(String client:authorizedClients) {
			this.template.convertAndSendToUser(client,"/queue/command", clientDTO, Map.of("type", clientDTO.getClass().getSimpleName()));
		}
	}

	private void sendReply(ClientDTO clientDTO) {
		for(String client:authorizedClients) {
			this.template.convertAndSendToUser(client,"/queue/reply", clientDTO, Map.of("type", clientDTO.getClass().getSimpleName()));
		}
	}
	
	public enum State {
		STARTED,
		STOPPED
	}
}
