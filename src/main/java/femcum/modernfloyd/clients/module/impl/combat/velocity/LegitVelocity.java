package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.MoveInputEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public final class LegitVelocity extends Mode<Velocity> {
    public final NumberValue chance = new NumberValue("Chance", this, 100, 0, 100, 1);
    public final BooleanValue legitTiming = new BooleanValue("Legit Timing", this, false);
    private boolean jump;

    public LegitVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        jump = false;
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (jump && MoveUtil.isMoving() && Math.random() * 100 < chance.getValue().doubleValue()) {
            event.setJump(true);
        }
    };

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress || event.isCancelled()) return;

        if (!mc.thePlayer.onGround) {
            return;
        }

        final Packet<?> p = event.getPacket();

        if (p instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) p;

            if (wrapper.getEntityID() == mc.thePlayer.getEntityId() && wrapper.motionY > 0 && (!legitTiming.getValue() || mc.thePlayer.ticksSinceVelocity <= 14 || mc.thePlayer.onGroundTicks <= 1)) {
                jump = true;
            }
        }
    };
}
