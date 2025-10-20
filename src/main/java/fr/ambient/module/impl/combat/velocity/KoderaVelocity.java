package fr.ambient.module.impl.combat.velocity;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.render.hud.Chat;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

public class KoderaVelocity extends ModuleMode {
    public KoderaVelocity(String modeName, Module module) {
        super(modeName, module);
    }
    public boolean gotvelo = false;

    @SubscribeEvent
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                gotvelo = true;
                event.setCancelled(true);
            }
        }

        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            if (!gotvelo) {
                event.setCancelled(true);
            }
            gotvelo = false;
        }
    }

    @SubscribeEvent
    private void onPacketSendEvent(PacketSendEvent event){
        if(event.getPacket() instanceof C03PacketPlayer c03PacketPlayer && gotvelo){
            c03PacketPlayer.onGround = true;
        }
    }
}
