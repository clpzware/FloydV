package femcum.modernfloyd.clients.module.impl.movement.wallclimb;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.WallClimb;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.util.MathHelper;

public class VulcanWallClimb extends Mode<WallClimb> {

    public VulcanWallClimb(String name, WallClimb parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.isCollidedHorizontally) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                event.setOnGround(true);
                mc.thePlayer.motionY = MoveUtil.jumpMotion();
            }

            final double yaw = MoveUtil.direction();
            event.setPosX(event.getPosX() - -MathHelper.sin((float) yaw) * 0.1f);
            event.setPosZ(event.getPosZ() - MathHelper.cos((float) yaw) * 0.1f);
        }
    };
}