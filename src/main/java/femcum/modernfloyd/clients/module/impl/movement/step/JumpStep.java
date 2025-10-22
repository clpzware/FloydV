package femcum.modernfloyd.clients.module.impl.movement.step;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.Step;
import femcum.modernfloyd.clients.value.Mode;

public class JumpStep extends Mode<Step> {

    public JumpStep(String name, Step parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedHorizontally) {
            mc.thePlayer.jump();
        }
    };
}