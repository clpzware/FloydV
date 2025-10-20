package fr.ambient.event.impl.render;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class EntityRenderEvent extends Event {
    private double x,y,z,yaw,pitch;
}
