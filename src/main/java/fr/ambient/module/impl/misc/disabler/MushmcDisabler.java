package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.misc.Disabler;
import net.minecraft.network.play.client.C03PacketPlayer;

public class MushmcDisabler extends ModuleMode {

    private float yaw;

    public MushmcDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer c03PacketPlayer) {
            if (c03PacketPlayer.getRotating()) {
                yaw = c03PacketPlayer.yaw;
            }

            if (((Disabler) this.getSuperModule()).timerBalance.getValue()) {
                if (!c03PacketPlayer.isMoving() && !c03PacketPlayer.getRotating()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
