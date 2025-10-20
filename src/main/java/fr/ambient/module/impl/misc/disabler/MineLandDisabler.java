package fr.ambient.module.impl.misc.disabler;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Flight;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;


public class MineLandDisabler extends ModuleMode {

    public MineLandDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onEnable() {
        ChatUtil.display("Void & Fly");
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (Ambient.getInstance().getModuleManager().getModule(Flight.class).isEnabled()) {
            if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
                event.setCancelled(true);
            }
        }
    }
}