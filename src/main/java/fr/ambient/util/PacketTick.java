package fr.ambient.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Getter
@Setter
@AllArgsConstructor
public class PacketTick {
    private long tick;
    private Packet packet;
}
