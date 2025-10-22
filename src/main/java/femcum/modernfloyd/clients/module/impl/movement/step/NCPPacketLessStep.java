package femcum.modernfloyd.clients.module.impl.movement.step;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.Step;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.potion.Potion;

public class NCPPacketLessStep extends Mode<Step> {

    private boolean step;

    public NCPPacketLessStep(String name, Step parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isPotionActive(Potion.jump)) {
            mc.thePlayer.jump();
            MoveUtil.stop();
            step = true;
        }

        if (mc.thePlayer.offGroundTicks == 3 && step) {
            mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
            MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() * 0.6 - Math.random() / 100f - 0.05);
            step = false;
        }
    };
}