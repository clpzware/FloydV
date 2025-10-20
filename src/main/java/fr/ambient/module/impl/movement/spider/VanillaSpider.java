package fr.ambient.module.impl.movement.spider;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class VanillaSpider extends ModuleMode {
    public VanillaSpider(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdateEvent(PreMotionEvent event) {
        if (mc.thePlayer.isCollidedHorizontally) {
            mc.thePlayer.jump();
        }
    }
}