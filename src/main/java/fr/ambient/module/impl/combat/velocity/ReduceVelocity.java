package fr.ambient.module.impl.combat.velocity;


import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Velocity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;


public class ReduceVelocity extends ModuleMode {


    private final Velocity velocity = (Velocity) this.getSuperModule();

    public ReduceVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent()
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled(false);
                mc.thePlayer.motionX *= 0.6;
                mc.thePlayer.motionZ *= 0.6;
            }
        }
    }



    @SubscribeEvent(EventPriority.LOW)
    private void onSendPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity && mc.thePlayer.hurtTime > 4) {
            mc.thePlayer.motionX *= 0.6;
            mc.thePlayer.motionZ *= 0.6;
        }
    }
}





