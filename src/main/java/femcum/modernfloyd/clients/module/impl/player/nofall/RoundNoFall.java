package femcum.modernfloyd.clients.module.impl.player.nofall;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.NoFall;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;

public class RoundNoFall extends Mode<NoFall> {

    public RoundNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        final double rounded = MoveUtil.roundToGround(event.getPosY());
        final float distance = FallDistanceComponent.distance;

        if (distance > 3 && Math.abs(rounded - event.getPosY()) < 0.005) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, rounded, mc.thePlayer.posZ);
            event.setOnGround(true);
            event.setPosY(rounded);
        }
    };
}