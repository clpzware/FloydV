package femcum.modernfloyd.clients.event;

import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public interface Event {
    default ScriptEvent<? extends Event> getScriptEvent() {
        return null;
    }
}
