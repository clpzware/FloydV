package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

public class TransactionLogger extends Module {
    public TransactionLogger() {
        super(11,"TransactionLogger", ModuleCategory.MISC);
    }

    @SubscribeEvent
    public void sendPacketEvent(PacketSendEvent event){
        if(event.getPacket() instanceof C0FPacketConfirmTransaction s32PacketConfirmTransaction) {
            ChatUtil.display(s32PacketConfirmTransaction.getUid() + " : " + s32PacketConfirmTransaction.getWindowId());
        }
    }
}
