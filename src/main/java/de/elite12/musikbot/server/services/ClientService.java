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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
@Service
@Controller
public class ClientService {

	@Autowired
	private SongService songService;

	@Autowired
	private StateService stateService;

	@Autowired
	private SimpMessagingTemplate template;

	private final Set<String> clients = new CopyOnWriteArraySet<>();
	
	private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

	public void notifynewSong() {
        logger.debug("Notify Song");
        if(isNotConnected()) return;
        if (this.stateService.getState().getState() == StateService.StateData.State.WAITING_FOR_SONGS) {
            this.sendSong(songService.getnextSong());
        }
    }
	
	public void pause() {
        logger.debug("Pausing");
        if(isNotConnected()) return;

        if (this.stateService.getState().getState() == StateService.StateData.State.PLAYING) {
        	this.stateService.updateState(
					stateData -> stateData.withState(StateService.StateData.State.PAUSED)
			);
        }
        else if (this.stateService.getState().getState() == StateService.StateData.State.PAUSED) {
			this.stateService.updateState(
					stateData -> stateData.withState(StateService.StateData.State.PLAYING)
			);
		}

		this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.PAUSE));
	}

    public void stop() {
        logger.debug("Stopping");
        if(isNotConnected()) return;

		StateService.StateData.State currentState = stateService.getState().getState();
        if (currentState == StateService.StateData.State.PLAYING || currentState == StateService.StateData.State.PAUSED) {

        	stateService.updateState(
					stateData -> stateData.withState(StateService.StateData.State.STOPPED)
			);

			this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.STOP));
        }
    }

    public void start() {
        logger.debug("Starting...");
        if(isNotConnected()) return;

        if (this.stateService.getState().getState() == StateService.StateData.State.STOPPED) {
			sendSong();
        }
    }

    public void sendVolume(short volume) {
		logger.debug("Sending Volume...");
		if(!isNotConnected()) {

			stateService.updateState(
					stateData -> stateData.withVolume(volume)
			);

			this.sendCommand(new VolumeCommand(volume));
		}
	}
    
    private void sendSong(Song song) {
        logger.debug("Sending Song...");
        if(isNotConnected()) return;

		stateService.updateState(
				stateData -> stateData
						.withState(StateService.StateData.State.PLAYING)
						.withSongTitle(song.getTitle())
						.withSongLink(song.getLink())
						.withProgressInfo(new StateService.StateData.ProgressInfo(Duration.ofSeconds(song.getDuration())))
		);

		this.sendCommand(new de.elite12.musikbot.shared.clientDTO.Song(song.getLink(),song.getTitle(), song.getLink().contains("spotify") ? "spotify" : "youtube"));
    }

    public void sendShutdown() {
        logger.debug("Sending Shutdown...");
        if(isNotConnected()) return;
        this.sendCommand(new SimpleCommand(SimpleCommand.CommandType.SHUTDOWN));
    }

	@MessageMapping("/client")
	private void onRequestSong(@Payload SimpleCommand command, @Header("simpSessionId") String sessionId) {
		if(!this.clients.contains(sessionId)) {
			this.sendCommand(new VolumeCommand(this.stateService.getState().getVolume()));
			this.clients.add(sessionId);
		}

		if(command.getCommand() == SimpleCommand.CommandType.REQUEST_SONG) {
			sendSong();
		}
	}

	private void sendSong() {
		Song song = songService.getnextSong();
		if (song != null) {
			this.sendSong(song);
		} else {
			stateService.updateState(
					stateData -> stateData.withState(StateService.StateData.State.WAITING_FOR_SONGS)
			);
		}
	}

	@EventListener
	public void onDisconnectEvent(SessionDisconnectEvent event) {
		if (this.clients.contains(event.getSessionId())) {
			this.clients.remove(event.getSessionId());

			if (this.isNotConnected()) {
				stateService.updateState(
						stateData -> stateData.withState(StateService.StateData.State.NOT_CONNECTED)
				);
			}
		}
	}
    
    private boolean isNotConnected() {
    	return this.clients.isEmpty();
    }

    private void sendCommand(ClientDTO clientDTO) {
		this.template.convertAndSend("/topic/client", clientDTO, Map.of("type", clientDTO.getClass().getSimpleName()));
	}
}
