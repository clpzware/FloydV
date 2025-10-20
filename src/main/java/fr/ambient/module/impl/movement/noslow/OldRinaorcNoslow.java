package fr.ambient.module.impl.movement.noslow;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.NoSlowdown;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.BlockPos;


public class OldRinaorcNoslow extends ModuleMode {

    private final NoSlowdown noslow = (NoSlowdown) this.getSuperModule();

    public OldRinaorcNoslow(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onSendPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement && noslow.isAllowed() && mc.thePlayer.isUsingItem()) {
            if (c08PacketPlayerBlockPlacement.getPosition().equals(new BlockPos(-1, -1, -1))) {
                Packet<?> packet = event.getPacket();
                event.setCancelled(true);
                PacketUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                PacketUtil.sendPacketNoEvent(packet);
                PacketUtil.sendPacket(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            }
        }
    }
}