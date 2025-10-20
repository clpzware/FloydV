package fr.ambient.module.impl.player.nofall;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Grim117Nofall extends ModuleMode {



    public Grim117Nofall(String modeName, Module module) {
        super(modeName, module);
    }



    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (!mc.thePlayer.onGround && mc.thePlayer.fallDistance > 1f) {
            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY + 0.000000001, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            mc.thePlayer.fallDistance = 0f;
        }
    }
}