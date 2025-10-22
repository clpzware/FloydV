package femcum.modernfloyd.clients.event;

import java.io.IOException;

@FunctionalInterface
public interface Listener<Event> {
    void call(Event event) throws IOException;
}