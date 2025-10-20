package fr.ambient.event.impl.player.camera;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LookEvent extends Event {
    private float yaw, pitch;
}
