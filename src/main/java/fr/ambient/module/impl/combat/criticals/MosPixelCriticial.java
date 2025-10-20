package fr.ambient.module.impl.combat.criticals;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Criticals;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;


public class MosPixelCriticial extends ModuleMode {

    private final Criticals crit = (Criticals) this.getSuperModule();

    public MosPixelCriticial(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity attackPacket && attackPacket.getAction().equals(C02PacketUseEntity.Action.ATTACK)) {
            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY + 0.000000271875, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            crit.stopwatch.reset();
        }
    }
}