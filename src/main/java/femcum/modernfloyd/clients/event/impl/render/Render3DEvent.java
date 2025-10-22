package femcum.modernfloyd.clients.event.impl.render;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptRender3DEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Render3DEvent implements Event {

    private final float partialTicks;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptRender3DEvent(this);
    }
}
