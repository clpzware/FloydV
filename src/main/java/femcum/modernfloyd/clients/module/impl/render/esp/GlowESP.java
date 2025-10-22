package femcum.modernfloyd.clients.module.impl.render.esp;

import femcum.modernfloyd.clients.component.impl.render.ESPComponent;
import femcum.modernfloyd.clients.component.impl.render.espcomponent.api.ESPColor;
import femcum.modernfloyd.clients.component.impl.render.espcomponent.impl.PlayerGlow;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.render.ESP;
import femcum.modernfloyd.clients.value.Mode;

import java.awt.*;

public final class GlowESP extends Mode<ESP> {

    public GlowESP(String name, ESP parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        Color color = getTheme().getFirstColor();
        ESPComponent.add(new PlayerGlow(new ESPColor(color, color, color)));
    };
}