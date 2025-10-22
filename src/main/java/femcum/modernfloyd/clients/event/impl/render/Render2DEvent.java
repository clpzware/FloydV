package femcum.modernfloyd.clients.event.impl.render;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptRender2DEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@AllArgsConstructor
public final class Render2DEvent implements Event {

    private final ScaledResolution scaledResolution;
    private final float partialTicks;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptRender2DEvent(this);
    }
}
