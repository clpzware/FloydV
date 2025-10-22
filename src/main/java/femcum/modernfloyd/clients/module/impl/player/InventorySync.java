package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

@ModuleInfo(aliases = {"module.player.inventorysync.name"}, description = "module.player.inventorysync.description", category = Category.PLAYER)
public class InventorySync extends Module {

    public short action;

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S32PacketConfirmTransaction) {
            final S32PacketConfirmTransaction wrapper = (S32PacketConfirmTransaction) packet;
            final Container inventory = mc.thePlayer.inventoryContainer;

            if (wrapper.getWindowId() == inventory.windowId) {
                this.action = wrapper.getActionNumber();

                if (this.action > 0 && this.action < inventory.transactionID) {
                    inventory.transactionID = (short) (this.action + 1);
                }
            }
        }
    };
}