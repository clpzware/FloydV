package fr.ambient.module.impl.movement.jesus;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class JesusBullet extends ModuleMode {
    public JesusBullet(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdateEvent(PreMotionEvent event) {
        if (mc.thePlayer.isInWater() && MoveUtil.moving()) {
            MoveUtil.strafe(0.4f);
            mc.thePlayer.motionY = 0.42f;
        }
    }
}