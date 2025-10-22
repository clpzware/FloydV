package femcum.modernfloyd.clients.event.impl.motion;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptPostMotionEvent;

public final class PostMotionEvent implements Event {
    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptPostMotionEvent(this);
    }
}
