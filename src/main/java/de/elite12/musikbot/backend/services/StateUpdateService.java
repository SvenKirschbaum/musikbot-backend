package de.elite12.musikbot.backend.services;

import de.elite12.musikbot.backend.api.dto.StatusUpdate;
import de.elite12.musikbot.backend.data.entity.Song;
import de.elite12.musikbot.backend.data.repository.SongRepository;
import de.elite12.musikbot.backend.events.GapcloserUpdateEvent;
import de.elite12.musikbot.backend.events.PlaylistChangedEvent;
import de.elite12.musikbot.backend.events.StateUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

@Service
public class StateUpdateService {

    private final Logger logger = LoggerFactory.getLogger(StateUpdateService.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private StateService stateService;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private GapcloserService gapcloserService;

    private FluxSink<ApplicationEvent> debounce;

    public StateUpdateService() {
        Flux.<ApplicationEvent>create(fluxSink -> debounce = fluxSink)
                .sample(Duration.ofMillis(20))
                .subscribe(event -> {
                    try {
                        StateService.StateData newState;
                        if (event instanceof StateUpdateEvent) newState = ((StateUpdateEvent) event).getNewState();
                        else newState = this.stateService.getState();

                        template.convertAndSend("/topic/state", this.getStateUpdate(newState));
                    } catch (Exception e) {
                        logger.error("Error sending state update", e);
                    }
                });
    }

    @EventListener
    public void onStateUpdate(StateUpdateEvent event) {
        debounce.next(event);
    }

    @EventListener
    public void onPlaylistChange(PlaylistChangedEvent event) {
        debounce.next(event);
    }

    @EventListener
    public void onGapcloserUpdate(GapcloserUpdateEvent event) {
        debounce.next(event);
    }

    public StatusUpdate getStateUpdate() {
        return this.getStateUpdate(this.stateService.getState());
    }


    public StatusUpdate getStateUpdate(StateService.StateData stateData) {
        StatusUpdate st = new StatusUpdate();

        st.setStatus(stateData.getState().toString());
        st.setSongtitle(stateData.getSongTitle());
        st.setSonglink(stateData.getSongLink());
        st.setVolume(stateData.getVolume());

        int duration = 0;
        ArrayList<Song> list = new ArrayList<>(30);

        Iterable<Song> songs = this.songRepository.findByPlayedOrderBySort(false);

        for (Song s : songs) {
            duration += s.getDuration();
            list.add(s);
        }

        st.setPlaylist(list);
        st.setPlaylistdauer(duration / 60);

        StateService.StateData.ProgressInfo progressInfo = stateData.getProgressInfo();

        if (progressInfo != null) {
            StatusUpdate.SongProgress sp = new StatusUpdate.SongProgress();
            sp.setStart(progressInfo.getStart());
            sp.setCurrent(Instant.now());
            sp.setDuration(progressInfo.getDuration());
            sp.setPaused(progressInfo.isPaused());
            sp.setPrepausedDuration(progressInfo.getPrepausedDuration());
            st.setProgress(sp);
        }

        st.setPreview(gapcloserService.getPreview());

        return st;
    }
}
