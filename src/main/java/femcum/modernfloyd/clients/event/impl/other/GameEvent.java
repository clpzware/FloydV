package femcum.modernfloyd.clients.event.impl.other;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptGameEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class GameEvent extends CancellableEvent {
    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptGameEvent(this);
    }
}