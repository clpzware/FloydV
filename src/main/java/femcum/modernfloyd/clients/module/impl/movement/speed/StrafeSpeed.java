package femcum.modernfloyd.clients.module.impl.movement.speed;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.MoveInputEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Speed;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public final class StrafeSpeed extends Mode<Speed> {

    private final BooleanValue hurtBoost = new BooleanValue("Hurt Boost", this, false);
    private final NumberValue boostSpeed = new NumberValue("Boost Speed", this, 1, 0.1, 9.5, 0.1);

    public StrafeSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (!MoveUtil.isMoving()) {
            MoveUtil.stop();
            return;
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }

        if (hurtBoost.getValue() && mc.thePlayer.hurtTime == 9) {
            MoveUtil.strafe(boostSpeed.getValue().doubleValue());
        }

        MoveUtil.strafe();
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> event.setJump(false);
}
