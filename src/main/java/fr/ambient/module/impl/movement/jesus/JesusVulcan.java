package fr.ambient.module.impl.movement.jesus;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class JesusVulcan extends ModuleMode {
    public JesusVulcan(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdateEvent(PreMotionEvent event) {
        if (mc.thePlayer.isInWater() && MoveUtil.moving()) {
            MoveUtil.strafe(0.22);
            mc.thePlayer.motionY = 0.8f;
        }
    }
}