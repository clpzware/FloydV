package femcum.modernfloyd.clients.module.impl.movement.step;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.StepEvent;
import femcum.modernfloyd.clients.module.impl.movement.Step;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public class VanillaStep extends Mode<Step> {

    private final NumberValue height = new NumberValue("Height", this, 1, 1, 10, 0.1);
    private final BooleanValue reverse = new BooleanValue("Reverse", this, false);
    private final NumberValue timer = new NumberValue("Timer", this, 0.5, 0.1, 1, 0.1);

    public VanillaStep(String name, Step parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.thePlayer.stepHeight = this.height.getValue().floatValue();

        if (!reverse.getValue() || !PlayerUtil.isBlockUnder(height.getValue().floatValue() + mc.thePlayer.getEyeHeight()) || PlayerUtil.inLiquid()) {
            return;
        }

        if (mc.thePlayer.posY < mc.thePlayer.lastGroundY) {
            if (!mc.thePlayer.onGround && mc.thePlayer.offGroundTicks <= 1) {
                mc.thePlayer.motionY = -height.getValue().doubleValue();
            }
        }

        if (mc.thePlayer.offGroundTicks == 1 && mc.thePlayer.posY < mc.thePlayer.lastLastGroundY) {
            mc.timer.timerSpeed = timer.getValue().floatValue();
        }
    };

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink
    public final Listener<StepEvent> onStep = event -> {
        if (event.getHeight() > 0.6) {
            mc.timer.timerSpeed = timer.getValue().floatValue();
        }
    };
}