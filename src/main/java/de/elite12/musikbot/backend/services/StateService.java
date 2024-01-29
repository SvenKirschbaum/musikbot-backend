package de.elite12.musikbot.backend.services;

import de.elite12.musikbot.backend.data.entity.Setting;
import de.elite12.musikbot.backend.data.entity.Song;
import de.elite12.musikbot.backend.data.repository.SettingRepository;
import de.elite12.musikbot.backend.data.repository.SongRepository;
import de.elite12.musikbot.backend.events.StateUpdateEvent;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

@Service
public class StateService {

    @Autowired
    private SettingRepository settings;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private StateData stateData;

    public StateService() {
        stateData = new StateData(null, StateData.State.NOT_CONNECTED, null, (short) 38, null);
    }

    @PostConstruct
    public void postConstruct() {
        Optional<Setting> volume = settings.findById("volume");

        volume.ifPresent(setting -> this.stateData = this.stateData.withVolume(Short.parseShort(setting.getValue())));
    }

    public StateData getState() {
        return this.stateData;
    }

    /**
     * This method is used to modify the global Application State.
     * The update occurs synchronized, so two concurrent updates are processed in order.
     * You should use the current state provided to the update Function as base of the modification to prevent race conditions.
     *
     * @param stateUpdateFunction Function which gets the current State as parameter and returns the new modified state
     */
    public synchronized void updateState(Function<StateData, StateData> stateUpdateFunction) {
        StateData newState = stateUpdateFunction.apply(this.getState());

        if (newState.getVolume() != this.stateData.getVolume()) {
            Setting volumesetting = new Setting("volume", Short.toString(newState.getVolume()));
            settings.save(volumesetting);
        }

        if (newState.getState() == StateData.State.PAUSED && this.stateData.getState() == StateData.State.PLAYING) {
            this.getState().getProgressInfo().pause();
        }

        if (newState.getState() == StateData.State.PLAYING && this.stateData.getState() == StateData.State.PAUSED) {
            this.getState().getProgressInfo().unpause();
        }

        if (newState.getState() != StateData.State.PLAYING && newState.getState() != StateData.State.PAUSED) {
            newState = newState.withSongTitle(null).withSongLink(null).withProgressInfo(null);
        }

        if (this.getState().getState() == StateData.State.PLAYING) {
            if (newState.getState() == StateData.State.STOPPED || newState.getState() == StateData.State.NOT_CONNECTED) {
                Song last = songRepository.getLastSong();
                if (last != null) {
                    last.setSkipped(true);
                    songRepository.save(last);
                }
            }
        }

        StateData oldState = this.stateData;
        this.stateData = newState;
        if (!oldState.equals(newState))
            this.applicationEventPublisher.publishEvent(new StateUpdateEvent(this, oldState, newState));
    }

    @Getter
    @With
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode
    public static class StateData {
        private final String songTitle;
        private final State state;
        private final String songLink;
        private final short volume;
        private final ProgressInfo progressInfo;

        public enum State {
            NOT_CONNECTED,
            CONNECTED,
            PAUSED,
            STOPPED,
            PLAYING,
            WAITING_FOR_SONGS;


            @Override
            public String toString() {
                return switch (this) {
                    case NOT_CONNECTED -> "Keine Verbindung zum BOT";
                    case CONNECTED -> "Verbunden";
                    case PAUSED -> "Pausiert";
                    case STOPPED -> "Gestoppt";
                    case PLAYING -> "Playing";
                    case WAITING_FOR_SONGS -> "Warte auf neue Lieder";
                };
            }
        }

        @RequiredArgsConstructor
        @Getter
        @EqualsAndHashCode
        public static class ProgressInfo {
            private Instant start = Instant.now();
            private final Duration duration;
            private Duration prepausedDuration = Duration.ZERO;
            boolean paused = false;

            public void pause() {
                prepausedDuration = prepausedDuration.plus(Duration.between(start, Instant.now()));
                this.paused = true;
            }

            public void unpause() {
                this.start = Instant.now();
                this.paused = false;
            }
        }
    }
}
