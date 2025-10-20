package fr.ambient.module.impl.movement.noslow;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.NoSlowdown;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C09PacketHeldItemChange;


public class Grim117Noslow extends ModuleMode {

    private final NoSlowdown noslow = (NoSlowdown) this.getSuperModule();

    public Grim117Noslow(String modeName, Module module) {
        super(modeName, module);
    }



    @SubscribeEvent
    private void onNetworkUpdate(PreMotionEvent event) {
        if (mc.thePlayer.isUsingItem() &&  noslow.isAllowed()) {
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 2));
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }
}