package de.elite12.musikbot.backend.events;

import org.springframework.context.ApplicationEvent;

public class PlaylistChangedEvent extends ApplicationEvent {
    public PlaylistChangedEvent(Object source) {
        super(source);
    }
}
