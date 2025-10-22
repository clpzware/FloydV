package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public final class TickVelocity extends Mode<Velocity> {

    private final NumberValue tickVelocity = new NumberValue("Tick Velocity", this, 1, 1, 6, 1);

    public TickVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.hurtTime == 10 - tickVelocity.getValue().intValue()) {
            MoveUtil.stop();
        }
    };
}
