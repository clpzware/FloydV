package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.MoveInputEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public final class GroundVelocity extends Mode<Velocity> {

    private final NumberValue delay = new NumberValue("Delay", this, 1, 0, 20, 1);

    private int ticks;

    public GroundVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.ticksSinceVelocity == delay.getValue().intValue()) {
            mc.thePlayer.onGround = true;
        }
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> {
        if (mc.thePlayer.ticksSinceVelocity == delay.getValue().intValue() + 1) {
            event.setJump(false);
        }
    };
}
