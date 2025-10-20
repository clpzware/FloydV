package fr.ambient.util.packet;

import com.viaversion.viabackwards.protocol.v1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.packet.ServerboundPackets1_12;
import fr.ambient.Ambient;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

@UtilityClass
public class PacketUtil {
    @SuppressWarnings("rawtypes")
    public static void sendToServer(PacketWrapper packet, Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) {
        try {
            if (currentThread) {
                packet.sendToServer(packetProtocol, skipCurrentPipeline);
            } else {
                packet.scheduleSendToServer(packetProtocol, skipCurrentPipeline);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    @SuppressWarnings("rawtypes")
    public void sendPacket(Packet packet) {
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
    }

    @SuppressWarnings("rawtypes")
    public void sendPacketNoEvent(Packet packet) {
        Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
    }
    public void sendUseItem112(){
        final PacketWrapper useItem = PacketWrapper.create(ServerboundPackets1_12.USE_ITEM, Ambient.getInstance().getUserConnection());


    }
}