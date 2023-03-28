package de.elite12.musikbot.server.util;

import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.events.PlaylistChangedEvent;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class SongEntityListener {

    private final SongRepository songRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public SongEntityListener(@Lazy SongRepository songRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.songRepository = songRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PrePersist
    public void prePersist(Song o) {
        // If no sort is set, initialize it to a value at the end
        if (o.getSort() == null) {
            Long maxSort = songRepository.findMaxSort();
            if (maxSort == null) {
                o.setSort(100.0);
            } else {
                o.setSort(maxSort + 100.0);
            }
        }
    }

    @PostPersist
    public void postPersist(Song o) {
        this.applicationEventPublisher.publishEvent(new PlaylistChangedEvent(this));
    }

    @PostUpdate
    public void postUpdate(Song o) {
        this.applicationEventPublisher.publishEvent(new PlaylistChangedEvent(this));
    }

    @PostRemove
    public void postRemove(Song o) {
        this.applicationEventPublisher.publishEvent(new PlaylistChangedEvent(this));
    }
}
