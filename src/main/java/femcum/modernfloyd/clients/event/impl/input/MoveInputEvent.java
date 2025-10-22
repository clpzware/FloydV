package femcum.modernfloyd.clients.event.impl.input;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptMoveInputEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoveInputEvent implements Event {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptMoveInputEvent(this);
    }
}
