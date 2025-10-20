package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.player.MoveUtil;

public class VanillaSpeed extends ModuleMode {
    public VanillaSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.onGround && MoveUtil.moving()) {
            mc.thePlayer.motionY = 0.42;
        }
        MoveUtil.strafe(((Speed) this.getSuperModule()).speed.getValue());
    }
}
