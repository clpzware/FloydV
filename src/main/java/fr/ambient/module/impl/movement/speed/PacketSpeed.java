package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;


public class PacketSpeed extends ModuleMode {

    public PacketSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        var setting = ((Speed) this.getSuperModule());
        if (mc.thePlayer.ticksExisted % 10 != 0) return;
        for (int i = 0; i <= setting.packetAmount.getValue(); i++) {
            var x = (-Math.sin(Math.toRadians(mc.thePlayer.rotationYaw)) * setting.distancePerPacket.getValue()) * i;
            var z = (Math.cos(Math.toRadians(mc.thePlayer.rotationYaw)) * setting.distancePerPacket.getValue()) * i;
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
        }
        PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.prevPosX, mc.thePlayer.prevPosY, mc.thePlayer.prevPosZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
    }
    @SubscribeEvent
    private void onPacketSendEvent(PacketSendEvent event) {
        if (event.packet instanceof C03PacketPlayer) {
            event.setCancelled(true);
        }
    }
}
