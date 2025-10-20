package fr.ambient.module.impl.combat.velocity;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Velocity;
import net.minecraft.network.play.server.*;


public class NormalVelocity extends ModuleMode {


    private final Velocity velocity = (Velocity) this.getSuperModule();

    public NormalVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent()
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12) {
            if (s12.getEntityID() == mc.thePlayer.getEntityId()) {
                double horizontalMulti = velocity.h.getValue() / 100;
                double verticalMulti = velocity.v.getValue() / 100;

                if (horizontalMulti == 0) {
                    event.setCancelled(true);

                    if (verticalMulti > 0) {
                        mc.thePlayer.motionY = (s12.getMotionY() * verticalMulti) / 8000.0;
                    }
                } else {
                    event.setCancelled(false);
                    s12.motionX = (int) (horizontalMulti * s12.motionX);
                    s12.motionY = (int) (verticalMulti * s12.motionY);
                    s12.motionZ = (int) (horizontalMulti * s12.motionZ);
                }
            }
        }
    }
}