package femcum.modernfloyd.clients.module.impl.player.scaffold.tower;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;

public class VanillaTower extends Mode<Scaffold> {

    public VanillaTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.blockNear(2)) {
            mc.thePlayer.motionY = 0.42F;
        }
    };
}
