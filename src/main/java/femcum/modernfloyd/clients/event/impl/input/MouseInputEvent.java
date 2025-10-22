package femcum.modernfloyd.clients.event.impl.input;

import femcum.modernfloyd.clients.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class MouseInputEvent implements Event {
    int mouseCode;
}
