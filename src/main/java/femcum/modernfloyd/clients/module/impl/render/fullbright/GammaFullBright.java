package femcum.modernfloyd.clients.module.impl.render.fullbright;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.TickEvent;
import femcum.modernfloyd.clients.module.impl.render.FullBright;
import femcum.modernfloyd.clients.value.Mode;

public final class GammaFullBright extends Mode<FullBright> {

    private float oldGamma;

    public GammaFullBright(String name, FullBright parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        mc.gameSettings.gammaSetting = 100.0F;
    };

    @Override
    public void onEnable() {
        oldGamma = mc.gameSettings.gammaSetting;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = oldGamma;
    }
}