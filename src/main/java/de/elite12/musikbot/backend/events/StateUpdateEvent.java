package de.elite12.musikbot.backend.events;

import de.elite12.musikbot.backend.services.StateService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class StateUpdateEvent extends ApplicationEvent {

    private StateService.StateData oldState;
    private StateService.StateData newState;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public StateUpdateEvent(Object source, StateService.StateData oldState, StateService.StateData newState) {
        super(source);

        this.oldState = oldState;
        this.newState = newState;
    }
}
