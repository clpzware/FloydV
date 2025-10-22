package femcum.modernfloyd.clients.module.impl.movement.flight;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import org.apache.commons.lang3.RandomUtils;

public class OldNCPFlight extends Mode<Flight> {

    public OldNCPFlight(String name, Flight parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        event.setPosY(event.getPosY() + 1E-5 + (mc.thePlayer.ticksExisted % 2 == 0 ? RandomUtils.nextDouble(1E-10, 1E-5) : -RandomUtils.nextDouble(1E-10, 1E-5)));
        mc.thePlayer.motionY = 0;
    };

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        event.setSpeed(MoveUtil.getAllowedHorizontalDistance(), Math.random() / 2000);
    };
}