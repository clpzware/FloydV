package femcum.modernfloyd.clients.module.impl.movement.speed;

import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Speed;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.Mode;

public class IntaveSpeed extends Mode<Speed> {

    public IntaveSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreUpdateEvent> preUpdate = event -> {
        mc.timer.timerSpeed = 1.009f;

        if (!mc.thePlayer.onGround) {
            RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw + 45, mc.thePlayer.rotationPitch), 10, MovementFix.NORMAL);
        }

        MoveUtil.moveFlying(0.000499);
    };

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<StrafeEvent> strafe = event -> {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }
    };
}
