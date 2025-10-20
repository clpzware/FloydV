package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

public class VerusCombat extends ModuleMode {

    public boolean a;

    public VerusCombat(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPacketSend(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            event.setCancelled(true);
            int transaction = a ? 1 : -1;
            PacketUtil.sendPacketNoEvent(new C0FPacketConfirmTransaction(transaction, (short) transaction, a));
            a = transaction == 1;
        }
    }
}
