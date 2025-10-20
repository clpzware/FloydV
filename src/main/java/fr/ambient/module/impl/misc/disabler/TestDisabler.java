package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.network.play.client.*;

public class TestDisabler extends ModuleMode {

    public TestDisabler(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPacketSend(PacketSendEvent event) {


        float forwardSpeed = mc.thePlayer.movementInput.moveForward;
        float strafeSpeed = mc.thePlayer.movementInput.moveStrafe;

        boolean jumping = mc.thePlayer.movementInput.jump;
        boolean sneaking = mc.thePlayer.movementInput.sneak;

        if (event.packet instanceof C03PacketPlayer) {
            if (MoveUtil.moving()) {
                PacketUtil.sendPacket(new C0CPacketInput(strafeSpeed, forwardSpeed, jumping, sneaking));
            }
        }
    }
}