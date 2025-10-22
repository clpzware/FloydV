package femcum.modernfloyd.clients.component.impl.viamcp;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;

public final class InteractEntityFixComponent extends Component {

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (!event.isCancelled() && ViaLoadingBase.getInstance()
                .getTargetVersion().newerThan(ProtocolVersion.v1_8)) {

            if (event.getPacket() instanceof C02PacketUseEntity) {
                C02PacketUseEntity use = ((C02PacketUseEntity) event.getPacket());

                event.setCancelled(event.isCancelled() || !use.getAction().equals(C02PacketUseEntity.Action.ATTACK));
            }
        }
    };

}