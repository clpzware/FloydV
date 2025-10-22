package femcum.modernfloyd.clients.module.impl.movement.longjump;

import femcum.modernfloyd.clients.component.impl.player.PacketlessDamageComponent;
import femcum.modernfloyd.clients.component.impl.player.PingSpoofComponent;
import femcum.modernfloyd.clients.component.impl.render.PercentageComponent;
import femcum.modernfloyd.clients.event.EventBusPriorities;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.impl.movement.LongJump;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class WatchdogLongJump extends Mode<LongJump> {

    public WatchdogLongJump(String name, LongJump parent) {
        super(name, parent);
    }
    @Override
    public void onEnable() {
        PacketlessDamageComponent.setActive(1f);
    }

    @EventLink(EventBusPriorities.LOW)
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (event.isCancelled()) return;

        final Packet<?> p = event.getPacket();

        if (p instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) p;

            if (wrapper.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled();
                mc.thePlayer.motionY = wrapper.getMotionY() / 8000.0D;

                mc.thePlayer.ticksSinceVelocity = 0;
            }
        }
    };

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        PercentageComponent.setActive((float) (PacketlessDamageComponent.isActive() ? PacketlessDamageComponent.getJumps() : 6) / 6);

        if (PacketlessDamageComponent.isActive()) {
            MoveUtil.stop();
        } else {
            if (mc.thePlayer.onGround) {
                MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100);
                mc.thePlayer.jump();
            }

            event.setOnGround(false);

            if (mc.thePlayer.offGroundTicks == 1) {
                mc.timer.timerSpeed = 0.2f;
                event.setOnGround(true);
            }

            if (mc.thePlayer.offGroundTicks <= 5 && mc.thePlayer.offGroundTicks >= 1) {
                PingSpoofComponent.spoof(99999, true, true, false, false, true);
            }

            if (mc.thePlayer.ticksSinceVelocity <= 20 && mc.thePlayer.ticksSinceVelocity > 1) {
                mc.thePlayer.motionY += 0.0239;

                MoveUtil.moveFlying(0.0039);
            }
        }
    };
}