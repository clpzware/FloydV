package fr.ambient.event.impl.player.move;

import fr.ambient.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class SlowDownEvent extends CancellableEvent {
    private float forward, strafe;
}
