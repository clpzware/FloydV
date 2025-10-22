package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.other.ServerKickEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptServerKickEvent extends ScriptEvent<ServerKickEvent> {

    public ScriptServerKickEvent(final ServerKickEvent wrappedEvent) {
        super(wrappedEvent);
    }

    public String[] getReason() {
        return wrapped.getMessage().toArray(new String[0]);
    }

    @Override
    public String getHandlerName() {
        return "onServerKick";
    }
}
