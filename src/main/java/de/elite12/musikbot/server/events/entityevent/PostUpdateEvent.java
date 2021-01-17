package de.elite12.musikbot.server.events.entityevent;

import de.elite12.musikbot.server.events.EntityEvent;

public class PostUpdateEvent<T> extends EntityEvent<T> {

    /**
     * Create a new {@code PostUpdateEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param entity the entity for which the event occured
     */
    public PostUpdateEvent(Object source, T entity) {
        super(source, entity);
    }
}
