package femcum.modernfloyd.clients.event.impl.motion;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptSlowDownEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SlowDownEvent extends CancellableEvent {
    private float strafeMultiplier;
    private float forwardMultiplier;
    private boolean useItem;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptSlowDownEvent(this);
    }
}
