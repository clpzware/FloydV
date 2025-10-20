package fr.ambient.module.impl.combat.velocity;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import net.minecraft.network.play.server.S12PacketEntityVelocity;


public class BlockmcVelocity extends ModuleMode {
    public BlockmcVelocity(String modeName, Module module) {
        super(modeName, module);
    }



    @SubscribeEvent()
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled(true);

                PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    }
}