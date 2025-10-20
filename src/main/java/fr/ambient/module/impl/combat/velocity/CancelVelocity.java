package fr.ambient.module.impl.combat.velocity;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Velocity;
import net.minecraft.network.play.server.*;


public class CancelVelocity extends ModuleMode {


    private final Velocity velocity = (Velocity) this.getSuperModule();

    public CancelVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent()
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled(true);


                mc.thePlayer.motionX += (s12PacketEntityVelocity.getMotionX() / 8000D) * (velocity.h.getValue() / 100);
                mc.thePlayer.motionZ += (s12PacketEntityVelocity.getMotionZ() / 8000D) *(velocity.h.getValue() / 100);
                mc.thePlayer.motionY = (s12PacketEntityVelocity.getMotionY() / 8000D) * (velocity.v.getValue() / 100);
            }
        }
    }
}