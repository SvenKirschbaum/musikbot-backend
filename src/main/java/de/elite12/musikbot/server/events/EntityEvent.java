package de.elite12.musikbot.server.events;

import jakarta.persistence.Entity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

@Getter
public abstract class EntityEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

    private final T entity;

    /**
     * Create a new {@code EntityEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param entity the entity for which the event occured
     */
    public EntityEvent(Object source, T entity) {
        super(source);

        if (!entity.getClass().isAnnotationPresent(Entity.class))
            throw new RuntimeException("EntityEvent can only be used with entities");

        this.entity = entity;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(this.getClass(), entity.getClass());
    }
}
