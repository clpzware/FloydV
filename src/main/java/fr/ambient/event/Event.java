package fr.ambient.event;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Event {
    @Getter
    @Setter
    private EventStage stage;
    @Getter
    @Setter
    private String name = "";
    protected Event(final EventStage stage) {
        this.stage = stage;
        this.name = "";
    }

    protected Event() {
        this.stage = EventStage.NONE;
        this.name = "";
    }
    public boolean isNamed(){
        return !Objects.equals(name, "");
    }



}
