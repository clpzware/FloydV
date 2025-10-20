package fr.ambient.module.impl.player.scaffold.tower;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class VulcanVanillaTower extends ModuleMode {
    private double jump;

    public VulcanVanillaTower(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onMove(MovementEvent event) {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {

            if (mc.thePlayer.onGround) {
                jump = mc.thePlayer.posY;
            } else if (mc.thePlayer.posY - jump >= 0.42) {
                mc.thePlayer.motionY = -0.95;
            }
            MoveUtil.strafe(0.24);
        }
    }
}