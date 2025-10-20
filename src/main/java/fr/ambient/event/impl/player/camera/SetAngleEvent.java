package fr.ambient.event.impl.player.camera;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SetAngleEvent extends Event {
    private float yaw, pitch;


}
