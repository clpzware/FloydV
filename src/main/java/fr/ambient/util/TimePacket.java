package fr.ambient.util;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.Packet;

@Getter
@AllArgsConstructor
public class TimePacket {

    private long time;
    private Packet packet;

}
