package fr.ambient.event.impl.render;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public final class Render3DEvent extends Event {
    private final float partialTicks;
}