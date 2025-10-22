package femcum.modernfloyd.clients.module.impl.movement.wallclimb;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.WallClimb;
import femcum.modernfloyd.clients.value.Mode;

public class VerusWallClimb extends Mode<WallClimb> {

    public VerusWallClimb(String name, WallClimb parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.isCollidedHorizontally) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.thePlayer.jump();
            }
        }
    };
}