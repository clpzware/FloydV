package femcum.modernfloyd.clients.component.impl.viamcp;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class FlyingPacketFixComponent extends Component {

    private boolean lastGround;

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
            final Packet<?> packet = event.getPacket();

            if (packet instanceof C03PacketPlayer) {
                final C03PacketPlayer wrapper = ((C03PacketPlayer) packet);

                if (!wrapper.isMoving() && !wrapper.isRotating() && wrapper.onGround == this.lastGround) {
                    event.setCancelled();
                }

                this.lastGround = wrapper.onGround;
            }
        }
    };
}
