package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.input.ClickEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptClickEvent extends ScriptEvent<ClickEvent> {

    public ScriptClickEvent(final ClickEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onClick";
    }
}
