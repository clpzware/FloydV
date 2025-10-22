package femcum.modernfloyd.clients.module.impl.player.scaffold.tower;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.value.Mode;

public class LegitTower extends Mode<Scaffold> {
    // Bypasses jump delay, holding down space is slower than this
    public LegitTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            return;
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }
    };
}
