package fr.ambient.event.impl.network;

import fr.ambient.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Getter
@Setter
@AllArgsConstructor
public class ReceivePacketEventFirst extends CancellableEvent {

    public Packet<?> packet;

}
