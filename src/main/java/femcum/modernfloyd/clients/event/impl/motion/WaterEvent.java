package femcum.modernfloyd.clients.event.impl.motion;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptWaterEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WaterEvent implements Event {
    private boolean water;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptWaterEvent(this);
    }
}
