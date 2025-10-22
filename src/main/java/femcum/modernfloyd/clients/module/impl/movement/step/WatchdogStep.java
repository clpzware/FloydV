package femcum.modernfloyd.clients.module.impl.movement.step;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostStrafeEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.impl.movement.Step;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

public class WatchdogStep extends Mode<Step> {
    private boolean step;

    public WatchdogStep(String name, Step parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
    };

    @EventLink
    public final Listener<PreMotionEvent> preMotionEventListener = event -> {
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
            step = true;
        }

        if (PlayerUtil.shouldStep()) {
            this.mc.thePlayer.stepHeight = 1.0F;
        }

    };

    @EventLink
    private final Listener<PacketSendEvent> packetSendEventListener = event -> {
        final double[] values = new double[] {0.42, 0.75, 1.0};

        if (event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition && PlayerUtil.shouldStep()) {
            for (double value : values) {
                event.setPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX - value, mc.thePlayer.posY + value, mc.thePlayer.posZ + value, false));
            }
        }

    };

    @EventLink
    public final Listener<PostStrafeEvent> postStrafeEventListener = postStrafeEvent -> {
        if (step) {
            this.mc.thePlayer.jump();
            step = false;
        }
    };
}