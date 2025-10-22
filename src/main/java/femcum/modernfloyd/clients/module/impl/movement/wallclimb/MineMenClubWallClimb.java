package femcum.modernfloyd.clients.module.impl.movement.wallclimb;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.WallClimb;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;

public class MineMenClubWallClimb extends Mode<WallClimb> {

    private boolean hitHead;

    public MineMenClubWallClimb(String name, WallClimb parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.isCollidedHorizontally && !hitHead && mc.thePlayer.ticksExisted % 3 == 0) {
            mc.thePlayer.motionY = MoveUtil.jumpMotion();
        }

        if (mc.thePlayer.isCollidedVertically) {
            hitHead = !mc.thePlayer.onGround;
        }
    };
}