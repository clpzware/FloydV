package fr.ambient.module.impl.movement.spider;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class VerusSpider extends ModuleMode {
    public VerusSpider(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdateEvent(PreMotionEvent event) {
        if (mc.thePlayer.isCollidedHorizontally) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.thePlayer.jump();
            }
        }
    }
}
