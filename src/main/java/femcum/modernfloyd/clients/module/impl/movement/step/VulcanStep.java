package femcum.modernfloyd.clients.module.impl.movement.step;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.StepEvent;
import femcum.modernfloyd.clients.module.impl.movement.Step;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

public class VulcanStep extends Mode<Step> {

    public VulcanStep(String name, Step parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6F;
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.ticksSinceJump > 11) {
            mc.thePlayer.stepHeight = 1;
        } else {
            mc.thePlayer.stepHeight = 0.6F;
        }
    };

    @EventLink
    public final Listener<StepEvent> onStep = event -> {
        if (event.getHeight() > 0.6) {
            mc.timer.timerSpeed = 0.5f;
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5F, mc.thePlayer.posZ, true));
        }
    };
}