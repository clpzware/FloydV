package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.misc.Disabler;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

public class BasicPacket extends ModuleMode {

    public BasicPacket(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (((Disabler) this.getSuperModule()).basicpacket.isSelected("Transactions (C -> S)") && event.getPacket() instanceof C0FPacketConfirmTransaction) {
            event.setCancelled(true);
        }

        if (((Disabler) this.getSuperModule()).basicpacket.isSelected("KeepAlive (C -> S)") && event.getPacket() instanceof C00PacketKeepAlive) {
            event.setCancelled(true);
        }

        if (event.getPacket() instanceof C0BPacketEntityAction c0BPacketEntityAction) {
            if (c0BPacketEntityAction.getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                event.setCancelled(((Disabler) this.getSuperModule()).basicpacket.isSelected("Start Sprint (C -> S)"));
            } else {
                event.setCancelled(((Disabler) this.getSuperModule()).basicpacket.isSelected("Stop Sprint (C -> S)"));
            }
        }
    }
}
