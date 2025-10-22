package femcum.modernfloyd.clients.module.impl.player.antivoid;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.AntiVoid;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public class CollisionAntiVoid extends Mode<AntiVoid> {

    private final NumberValue distance = new NumberValue("Distance", this, 5, 0, 10, 1);

    public CollisionAntiVoid(String name, AntiVoid parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (FallDistanceComponent.distance > distance.getValue().intValue() && !PlayerUtil.isBlockUnder() && mc.thePlayer.posY + mc.thePlayer.motionY < Math.floor(mc.thePlayer.posY)) {
            mc.thePlayer.motionY = Math.floor(mc.thePlayer.posY) - mc.thePlayer.posY;
            if (mc.thePlayer.motionY == 0) {
                mc.thePlayer.onGround = true;
                event.setOnGround(true);
            }
        }
    };
}