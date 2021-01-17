package de.elite12.musikbot.server.services;

import de.elite12.musikbot.server.api.dto.StatusUpdate;
import de.elite12.musikbot.server.data.entity.Setting;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SettingRepository;
import de.elite12.musikbot.server.data.repository.SongRepository;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

@Service
public class StateService {

    @Autowired
    private SettingRepository settings;

    @Autowired
    private PushService pushService;

    @Autowired
    private SongRepository songRepository;

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

    public synchronized void updateState(StateData newState) {
        if(newState.getVolume() != this.stateData.getVolume()) {
            Setting volumesetting = new Setting("volume", Short.toString(newState.getVolume()));
            settings.save(volumesetting);
        }

        if (newState.getState() == StateData.State.PAUSED && this.stateData.getState() == StateData.State.PLAYING) {
            this.getState().getProgressInfo().pause();
        }

        if (newState.getState() == StateData.State.PLAYING && this.stateData.getState() == StateData.State.PAUSED) {
            this.getState().getProgressInfo().unpause();
        }

        if (this.getState().getState() == StateData.State.PLAYING) {
            if (newState.getState() == StateData.State.STOPPED || newState.getState() == StateData.State.NOT_CONNECTED) {
                Song last = songRepository.getLastSong();
                if (last != null) {
                    last.setSkipped(true);
                    songRepository.save(last);
                }
                newState = newState.withSongTitle(null).withSongLink(null).withProgressInfo(null);
            }
            if (newState.getState() == StateData.State.WAITING_FOR_SONGS) {
                newState = newState.withSongTitle(null).withSongLink(null).withProgressInfo(null);
            }
        }

        this.stateData = newState;
        this.pushService.sendState();
    }

    public StatusUpdate getStateUpdate() {
        StatusUpdate st = new StatusUpdate();

        st.setStatus(this.getState().getState().toString());
        st.setSongtitle(this.getState().getSongTitle());
        st.setSonglink(this.getState().getSongLink());
        st.setVolume(this.getState().getVolume());

        int duration = 0;
        ArrayList<Song> list = new ArrayList<>(30);

        Iterable<Song> songs = this.songRepository.findByPlayedOrderBySort(false);

        for(Song s: songs) {
            duration += s.getDuration();
            list.add(s);
        }

        //Resort songs because if a song has been added just now the sort field hasnt been persisted to the database yet, and is therefore not respected by the repository query
        list.sort(Comparator.comparingLong(Song::getSort));

        st.setPlaylist(list);
        st.setPlaylistdauer(duration / 60);

        StateData.ProgressInfo progressInfo = this.getState().getProgressInfo();

        if(progressInfo != null) {
            StatusUpdate.SongProgress sp = new StatusUpdate.SongProgress();
            sp.setStart(progressInfo.getStart());
            sp.setCurrent(Instant.now());
            sp.setDuration(progressInfo.getDuration());
            sp.setPaused(progressInfo.isPaused());
            sp.setPrepausedDuration(progressInfo.getPrepausedDuration());
            st.setProgress(sp);
        }

        return st;
    }

    @Getter
    @With
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
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
