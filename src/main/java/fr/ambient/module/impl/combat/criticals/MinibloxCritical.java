package fr.ambient.module.impl.combat.criticals;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Criticals;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;


public class MinibloxCritical extends ModuleMode {

    private final Criticals crit = (Criticals) this.getSuperModule();

    public MinibloxCritical(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity attackPacket && attackPacket.getAction().equals(C02PacketUseEntity.Action.ATTACK)) {
            if (KillAura.target.getHurtTime() == 0 && mc.thePlayer.fallDistance < 2.5 && !mc.thePlayer.capabilities.allowFlying) {
                if (mc.thePlayer.onGround) {
                    PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY + 0.0625, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                }
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            }
        }
    }

    @SubscribeEvent
    private void onPlayerNetworkTick(PreMotionEvent event) {
        if (KillAura.target != null && mc.thePlayer.fallDistance < 2.5) {
            event.setPosY(event.getPosY() + 0.1);
        }
    }
}