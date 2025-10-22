package femcum.modernfloyd.clients.event.impl.input;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptClickEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public final class ClickEvent extends CancellableEvent {
    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptClickEvent(this);
    }
}
