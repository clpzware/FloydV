package femcum.modernfloyd.clients.event.impl.motion;

import femcum.modernfloyd.clients.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class SafeWalkEvent implements Event {
    private double height;
}
