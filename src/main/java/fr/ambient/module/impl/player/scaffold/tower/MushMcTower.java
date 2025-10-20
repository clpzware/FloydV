package fr.ambient.module.impl.player.scaffold.tower;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class MushMcTower extends ModuleMode {

    public MushMcTower(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onMove(MovementEvent event) {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            switch (mc.thePlayer.airTicks) {
                case 0 -> {
                    MoveUtil.strafe(0.26f, 0.25f, 0.27f);
                    event.setY(0.42f);
                    mc.thePlayer.motionY = event.getY();
                }
                case 2 -> {
                    mc.thePlayer.airTicks = -1;
                    event.setY(1 - mc.thePlayer.posY % 1);
                    mc.thePlayer.motionY = event.getY();
                }
            }
            if (!MoveUtil.moving() && mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY *= 1.2;
            }
        }
    }
}
