package femcum.modernfloyd.clients.event.impl.packet;

import femcum.modernfloyd.clients.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

@Getter
@Setter
@AllArgsConstructor
public final class PacketReceiveEvent extends CancellableEvent {
    private Packet<?> packet;
    private NetworkManager networkManager;
}
