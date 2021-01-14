package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.shared.clientDTO.ClientDTO;
import de.elite12.musikbot.shared.clientDTO.SimpleCommand;
import de.elite12.musikbot.shared.clientDTO.VolumeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
@Service
@Controller
public class ClientService {

	@Autowired
	private SongService songservice;

	@Autowired
	private PushService pushService;

	@Autowired
	private SimpMessagingTemplate template;

	private final Set<String> clients = new CopyOnWriteArraySet<>();

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
				songservice.setState(SongService.State.PAUSED);
				songservice.getProgressInfo().pause();
			} else {
				songservice.setState(SongService.State.PLAYING);
				songservice.getProgressInfo().unpause();
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
            this.ispaused = false;

			songservice.setState(SongService.State.STOPPED);
			songservice.setSongtitle(null);
			songservice.setSonglink(null);
			songservice.setProgressInfo(null);

			this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.STOP));

			this.pushService.sendState();
        }
    }

    public void start() {
        logger.debug("Starting...");
        if(isNotConnected()) return;
        if (this.state == State.STOPPED) {
            this.state = State.STARTED;
			sendSong();
        }
    }

    public void sendVolume(short volume) {
		logger.debug("Sending Volume...");
		if(!isNotConnected()) {
			this.sendCommand(new VolumeCommand(volume));
			this.songservice.setVolume(volume);
		}
		this.pushService.sendState();
	}
    
    private void sendSong(Song song) {
        logger.debug("Sending Song...");
        if(isNotConnected()) return;

		this.waitforsong = false;
		songservice.setState(SongService.State.PLAYING);
		songservice.setSongtitle(song.getTitle());
		songservice.setSonglink(song.getLink());
		songservice.setProgressInfo(new SongService.ProgressInfo(Instant.now(), Duration.ofSeconds(song.getDuration()),Duration.ZERO, false));

		this.sendCommand(new de.elite12.musikbot.shared.clientDTO.Song(song.getLink(),song.getTitle(), song.getLink().contains("spotify") ? "spotify" : "youtube"));

		this.pushService.sendState();
    }

    public void sendShutdown() {
        logger.debug("Sending Shutdown...");
        if(isNotConnected()) return;
        this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.SHUTDOWN));
    }

	@MessageMapping("/client")
	private void onRequestSong(@Payload SimpleCommand command, @Header("simpSessionId") String sessionId) {
		if(!this.clients.contains(sessionId)) {
			this.sendCommand(new VolumeCommand(songservice.getVolume()));
			this.clients.add(sessionId);
		}

		if(command.getCommand() == SimpleCommand.CommandType.REQUEST_SONG) {
			sendSong();
		}
	}

	private void sendSong() {
		Song song = songservice.getnextSong();
		if (song != null) {
			this.sendSong(song);
		} else {
			songservice.setState(SongService.State.WAITING_FOR_SONGS);
			songservice.setSongtitle(null);
			songservice.setSonglink(null);
			songservice.setProgressInfo(null);
			this.waitforsong = true;
			this.pushService.sendState();
		}
	}

	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) {
		this.clients.remove(event.getSessionId());

		if(this.isNotConnected()) {
			songservice.setState(SongService.State.NOT_CONNECTED);
			songservice.setSonglink(null);
			songservice.setSongtitle(null);
			songservice.setProgressInfo(null);
			this.pushService.sendState();
		}
	}
    
    private boolean isNotConnected() {
    	return this.clients.isEmpty();
    }

    private void sendCommand(ClientDTO clientDTO) {
		this.template.convertAndSend("/topic/client", clientDTO, Map.of("type", clientDTO.getClass().getSimpleName()));
	}
	
	public enum State {
		STARTED,
		STOPPED
	}
}
