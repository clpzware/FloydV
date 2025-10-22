package femcum.modernfloyd.clients.event.impl.motion;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptHitSlowDownEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class HitSlowDownEvent extends CancellableEvent {
    public double slowDown;
    public boolean sprint;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptHitSlowDownEvent(this);
    }
}
