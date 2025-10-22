package femcum.modernfloyd.clients.event.impl.render;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class LookEvent implements Event {
    private Vector2f rotation;
}
