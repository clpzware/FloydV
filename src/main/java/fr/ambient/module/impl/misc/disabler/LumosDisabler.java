package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class LumosDisabler extends ModuleMode {

    private int tickCounter = 0;

    public LumosDisabler(String modeName, Module module) {
        super(modeName, module);
    }


    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
    }

    public void onEnable() {
        ChatUtil.display("Only work on Lumos src Leaks");
    }

    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {
        mc.timer.timerSpeed = 0.3f;
        if (tickCounter % 2 == 0) {
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer(false));
        }
        tickCounter++;
    }
}

