package femcum.modernfloyd.clients.script.api.wrapper.impl.event;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.ScriptWrapper;

public abstract class ScriptEvent<T extends Event> extends ScriptWrapper<T> {

    public ScriptEvent(final T wrappedEvent) {
        super(wrappedEvent);
    }

    public abstract String getHandlerName();
}
