package fr.ambient.module.impl.movement.noslow;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.NoSlowdown;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C09PacketHeldItemChange;


public class OldIntaveNoslow extends ModuleMode {

    private final NoSlowdown noslow = (NoSlowdown) this.getSuperModule();

    public OldIntaveNoslow(String modeName, Module module) {
        super(modeName, module);
    }



    @SubscribeEvent
    private void onSendPacket(PacketSendEvent event) {
        if (noslow.isAllowed() && mc.thePlayer.isUsingItem()) {
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }
}