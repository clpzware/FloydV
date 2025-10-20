package fr.ambient.module.impl.movement.jesus;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class JesusVerusFloat extends ModuleMode {
    public JesusVerusFloat(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdateEvent(PreMotionEvent event) {
        if (mc.thePlayer.isInWater() && MoveUtil.moving()) {
            if (mc.thePlayer.isCollidedHorizontally) {
                mc.thePlayer.motionY = 0.3;
                MoveUtil.strafe(0.1);
            } else {
                mc.thePlayer.motionY = 0;
            }
            MoveUtil.strafe(0.325f, 0.325f, 0.425f);
        }
    }
}