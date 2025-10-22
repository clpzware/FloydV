package femcum.modernfloyd.clients.module.impl.movement.step;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.StepEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.impl.movement.Step;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public class NewNCPStep extends Mode<Step> {

    private final NumberValue height = new NumberValue("Height", this, 1, 1, 1.5, 0.1);
    private final NumberValue timer = new NumberValue("Timer", this, 0.5, 0.1, 1, 0.1);

    public NewNCPStep(String name, Step parent) {
        super(name, parent);
    }

    private double stepHeight;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.onGround && !PlayerUtil.inLiquid()) {
            mc.thePlayer.stepHeight = this.height.getValue().floatValue();
        } else {
            mc.thePlayer.stepHeight = 0.6F;
        }
    };

    @EventLink
    public final Listener<StepEvent> onStep = event -> {
        if (!mc.thePlayer.onGround || PlayerUtil.inLiquid()) {
            return;
        }

        final double height = event.getHeight();
        this.stepHeight = height;

        if (height <= 0.6F) {
            return;
        }

        final double[] values;

        if (height > 1.015) {
            values = new double[] {0.42F, 0.7532F, 1.0F, 0.98F};
        } else if (height > 0.875) {
            values = new double[] {0.42F, 0.7532F, 1.0F};
        } else {
            values = new double[] {0.39F, 0.6938F};
        }

        mc.timer.timerSpeed = this.timer.getValue().floatValue();

        for (final double d : values) {
            // this flags "moving more packets"
            // because ur sending another movement packet - creida
             PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + d, mc.thePlayer.posZ, false));
        }
    };

    @EventLink
    private final Listener<PacketSendEvent> packetSendEventListener = packetSendEvent -> {
        final double[] values;

        if (this.stepHeight > 1.015) {
            values = new double[]{0.42F, 0.7532F, 1.0F, 0.98F};
        } else if (this.stepHeight > 0.875) {
            values = new double[]{0.42F, 0.7532F, 1.0F};
        } else {
            values = new double[]{0.39F, 0.6938F};
        }

        if (packetSendEvent.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition && PlayerUtil.shouldStep()) {
            for (final double d : values) {
              //  packetSendEvent.setPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + d, mc.thePlayer.posZ, true));
            }
        }

    };

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6F;
    }
}