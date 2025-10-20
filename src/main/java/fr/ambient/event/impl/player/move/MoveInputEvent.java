package fr.ambient.event.impl.player.move;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoveInputEvent extends Event {
    private float forward, strafe;
    private boolean jumping, sneaking;
    private double sneakMultiplier;
}