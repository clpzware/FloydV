package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class MinemenSpeed extends ModuleMode {
    public MinemenSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {

        if (mc.thePlayer.onGround && MoveUtil.moving()) {
            MoveUtil.strafe(0.26);
            mc.thePlayer.motionY = 0.42;
        }
    }
}
