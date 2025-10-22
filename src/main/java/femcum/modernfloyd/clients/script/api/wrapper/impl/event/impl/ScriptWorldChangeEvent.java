package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptWorldChangeEvent extends ScriptEvent<WorldChangeEvent> {

    public ScriptWorldChangeEvent(final WorldChangeEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onWorldChange";
    }
}
