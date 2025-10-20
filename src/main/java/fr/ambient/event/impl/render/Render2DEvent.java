package fr.ambient.event.impl.render;

import fr.ambient.event.Event;
import lombok.Getter;

@Getter
public final class Render2DEvent extends Event {
    private final float partialTicks;

    public Render2DEvent(float partialTicks){
        this.partialTicks = partialTicks;
        this.setName("Render2DEvent");
    }

}