package femcum.modernfloyd.clients.module.impl.movement.speed;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Speed;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public class VanillaSpeed extends Mode<Speed> {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);

    public VanillaSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (MoveUtil.isMoving() && mc.thePlayer.onGround) {
            this.mc.thePlayer.jump();
        }

        event.setSpeed(speed.getValue().floatValue());
    };
}