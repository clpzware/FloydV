package fr.ambient.module.impl.combat.velocity;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

public class S32CancelVelocity extends ModuleMode {


    public boolean gotvelo = false;

    public S32CancelVelocity(String modeName, Module module) {
        super(modeName, module);
    }

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
}