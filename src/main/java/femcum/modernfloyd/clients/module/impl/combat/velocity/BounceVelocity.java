package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public final class BounceVelocity extends Mode<Velocity> {

    private final NumberValue tick = new NumberValue("Tick", this, 0, 0, 6, 1);
    private final BooleanValue vertical = new BooleanValue("Vertical", this, false);
    private final BooleanValue horizontal = new BooleanValue("Horizontal", this, false);

    public BounceVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.hurtTime == 9 - this.tick.getValue().intValue()) {
            if (this.horizontal.getValue()) {
                if (MoveUtil.isMoving()) {
                    MoveUtil.strafe();
                } else {
                    mc.thePlayer.motionZ *= -1;
                    mc.thePlayer.motionX *= -1;
                }
            }

            if (this.vertical.getValue()) {
                mc.thePlayer.motionY *= -1;
            }
        }
    };
}
