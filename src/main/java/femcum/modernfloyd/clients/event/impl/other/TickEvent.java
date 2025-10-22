package femcum.modernfloyd.clients.event.impl.other;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptTickEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class TickEvent extends CancellableEvent {
    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptTickEvent(this);
    }
}
