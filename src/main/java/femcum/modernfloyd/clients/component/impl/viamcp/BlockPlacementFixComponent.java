package femcum.modernfloyd.clients.component.impl.viamcp;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public final class BlockPlacementFixComponent extends Component {

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_11)) {
            final Packet<?> packet = event.getPacket();

            if (packet instanceof C08PacketPlayerBlockPlacement) {
                final C08PacketPlayerBlockPlacement wrapper = ((C08PacketPlayerBlockPlacement) packet);
                wrapper.facingX /= 16.0F;
                wrapper.facingY /= 16.0F;
                wrapper.facingZ /= 16.0F;
                event.setPacket(wrapper);
            }
        }
    };
}