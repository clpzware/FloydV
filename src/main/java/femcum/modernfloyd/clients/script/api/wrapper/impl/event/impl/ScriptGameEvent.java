package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.other.GameEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptGameEvent extends ScriptEvent<GameEvent> {

    public ScriptGameEvent(final GameEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onGame";
    }
}
