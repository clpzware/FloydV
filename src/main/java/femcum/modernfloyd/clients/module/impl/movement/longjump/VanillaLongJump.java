package femcum.modernfloyd.clients.module.impl.movement.longjump;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.LongJump;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public class VanillaLongJump extends Mode<LongJump> {

    private final NumberValue height = new NumberValue("Height", this, 0.5, 0.1, 1, 0.01);
    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);

    public VanillaLongJump(String name, LongJump parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = height.getValue().floatValue();
        }

        event.setSpeed(speed.getValue().floatValue());
    };

    @Override
    public void onDisable() {
        MoveUtil.stop();
    }
}