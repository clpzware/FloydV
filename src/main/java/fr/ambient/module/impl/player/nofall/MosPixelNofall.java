package fr.ambient.module.impl.player.nofall;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class MosPixelNofall extends ModuleMode {


    public MosPixelNofall(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.packet instanceof C03PacketPlayer) {
            if (mc.thePlayer.fallDistance > 3) {
                PacketUtil.sendPacket(new C03PacketPlayer(mc.thePlayer.ticksExisted % 2 == 0));
            }
        }
    }
}
