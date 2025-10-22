package femcum.modernfloyd.clients.event.impl.other;

import femcum.modernfloyd.clients.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class StepEvent implements Event {

    private double height;
}
