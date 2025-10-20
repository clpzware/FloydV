package fr.ambient.module.impl.player.scaffold.tower;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class VerusTower extends ModuleMode {

    public VerusTower(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onMove(MovementEvent event) {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 1;
            }
            if (mc.thePlayer.airTicks % 3 == 0) {
                MoveUtil.strafe(0.35f, 0.45f, 0.5f);
                mc.thePlayer.motionY = 0.7;
            }
        }
    }
}
