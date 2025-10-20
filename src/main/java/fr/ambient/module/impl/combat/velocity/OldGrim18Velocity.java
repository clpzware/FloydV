package fr.ambient.module.impl.combat.velocity;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

public class OldGrim18Velocity extends ModuleMode {

    private int tpcancel = 0;
    private int update = 0;

    public OldGrim18Velocity(String modeName, Module module) {
        super(modeName, module);
    }

    public void onEnable() {
        tpcancel = 0;
    }

    @SubscribeEvent
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCancelled(true);
                tpcancel = 6;
            }
        }

        if (event.getPacket() instanceof S32PacketConfirmTransaction && tpcancel > 0) {
            event.setCancelled(true);
            tpcancel--;
        }
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        update++;
        if (update >= 10) {
            update = 0;
            if (tpcancel > 0) {
                tpcancel--;
            }
        }
    }
}
