package fr.ambient.event.impl.player.move;

import fr.ambient.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JumpEvent extends CancellableEvent {
    private float jumpMotion, yaw;
    private boolean sprinting;
}