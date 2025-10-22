package femcum.modernfloyd.clients.module.impl.movement.speed;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.JumpEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Speed;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;

public class MineMenClubSpeed extends Mode<Speed> {
    public MineMenClubSpeed(String name, Speed parent) {
        super(name, parent);
    }

    private int jump = 0;
    @EventLink
    public final Listener<JumpEvent> onJumpEvent = event -> jump++;

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (jump % 2 == 1 && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.16477328182606651;
        }

        if (mc.thePlayer.hurtTime <= 2) {
            MoveUtil.strafe();
        }
    };

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }
    };
}