package de.elite12.musikbot.server.util;

import de.elite12.musikbot.server.events.entityevent.*;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GlobalEntityListener {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @PrePersist
    public void prePersist(Object o) {
        applicationEventPublisher.publishEvent(new PrePersistEvent<>(this, o));
    }

    @PreUpdate
    public void preUpdate(Object o) {
        applicationEventPublisher.publishEvent(new PreUpdateEvent<>(this, o));
    }

    @PreRemove
    public void preRemove(Object o) {
        applicationEventPublisher.publishEvent(new PreRemoveEvent<>(this, o));
    }

    @PostLoad
    public void postLoad(Object o) {
        applicationEventPublisher.publishEvent(new PostLoadEvent<>(this, o));
    }

    @PostRemove
    public void postRemove(Object o) {
        applicationEventPublisher.publishEvent(new PostRemoveEvent<>(this, o));
    }

    @PostUpdate
    public void postUpdate(Object o) {
        applicationEventPublisher.publishEvent(new PostUpdateEvent<>(this, o));
    }

    @PostPersist
    public void postPersist(Object o) {
        applicationEventPublisher.publishEvent(new PostPersistEvent<>(this, o));
    }
}
