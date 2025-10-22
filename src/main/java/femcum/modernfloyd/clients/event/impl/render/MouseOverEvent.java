package femcum.modernfloyd.clients.event.impl.render;

import femcum.modernfloyd.clients.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.MovingObjectPosition;

@Getter
@Setter
public class MouseOverEvent implements Event {

    public MouseOverEvent(double range, float expand) {
        this.range = range;
        this.expand = expand;
    }

    private double range;
    private float expand;
    private MovingObjectPosition movingObjectPosition;

}
