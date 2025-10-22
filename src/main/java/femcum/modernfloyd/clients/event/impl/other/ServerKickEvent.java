package femcum.modernfloyd.clients.event.impl.other;

import femcum.modernfloyd.clients.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public final class ServerKickEvent implements Event {
    public List<String> message;
}