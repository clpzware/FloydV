package femcum.modernfloyd.clients.module.impl.movement.longjump;

import femcum.modernfloyd.clients.component.impl.player.BlinkComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.LongJump;
import femcum.modernfloyd.clients.value.Mode;

public class MatrixLongJump extends Mode<LongJump> {

    private double lastMotion;
    private int ticks;

    public MatrixLongJump(String name, LongJump parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (!mc.thePlayer.onGround)
            ticks++;
        if (mc.thePlayer.onGround)
            mc.thePlayer.jump();
        if (ticks % 12 == 0 || mc.thePlayer.isCollidedVertically)
            lastMotion = mc.thePlayer.motionY;
        mc.thePlayer.motionY = lastMotion;
        if (mc.thePlayer.motionY < 0.1)
            getModule(LongJump.class).setEnabled(false);
    };

    @Override
    public void onEnable() {
        this.ticks = 0;
        this.lastMotion = 0;
        BlinkComponent.blinking = true;
    }

    @Override
    public void onDisable() {
        BlinkComponent.blinking = false;
    }
}