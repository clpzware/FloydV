package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public final class UniversoCraftVelocity extends Mode<Velocity> {

    public UniversoCraftVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress || event.isCancelled()) return;

        final Packet<?> p = event.getPacket();

        if (p instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) p;

            if (wrapper.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled();
                mc.thePlayer.motionY += 0.1 - Math.random() / 100f;
            }
        }

        if (p instanceof S27PacketExplosion) {
            event.setCancelled();
            mc.thePlayer.motionY += 0.1 - Math.random() / 100f;
        }
    };
}
