package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptPreUpdateEvent extends ScriptEvent<PreUpdateEvent> {

    public ScriptPreUpdateEvent(final PreUpdateEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onPreUpdate";
    }
}
