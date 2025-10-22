package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.other.KillEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.ScriptEntity;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptKillEvent extends ScriptEvent<KillEvent> {
    public ScriptEntity getEntity() {
        return new ScriptEntity(this.wrapped.getEntity());
    }

    public ScriptKillEvent(KillEvent wrappedEvent) {
        super(wrappedEvent);
    }

    @Override
    public String getHandlerName() {
        return "onKill";
    }
}
