package fr.ambient.module.impl.player.nofall;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class MinibloxNofall extends ModuleMode {


    public MinibloxNofall(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (mc.thePlayer.fallDistance > 2.5 && !mc.thePlayer.capabilities.allowFlying && (event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition || event.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook)) {
            event.setCancelled(true);
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.fallDistance * 1.2, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true));
        }
    }
}
