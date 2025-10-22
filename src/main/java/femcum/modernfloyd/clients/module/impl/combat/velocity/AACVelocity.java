package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.component.impl.player.BadPacketsComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.MoveInputEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.value.Mode;

public final class AACVelocity extends Mode<Velocity> {

    private boolean jump;

    public AACVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.hurtTime > 0 && !BadPacketsComponent.bad(false, true, false, false, false)) {
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
        }

        jump = false;
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (jump) {
            event.setJump(true);
        }
    };
}
