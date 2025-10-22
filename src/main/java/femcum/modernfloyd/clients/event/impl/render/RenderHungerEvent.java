package femcum.modernfloyd.clients.event.impl.render;

import femcum.modernfloyd.clients.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

@Getter
@AllArgsConstructor
public final class RenderHungerEvent implements Event {

    private final ScaledResolution scaledResolution;

}
