package fr.ambient.module.impl.movement.spider;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class VulcanSpider extends ModuleMode {
    public VulcanSpider(String modeName, Module module) {
        super(modeName, module);
    }
    boolean wasFlying = false;
    @SubscribeEvent
    private void onUpdateEvent(PreMotionEvent event) {
        if (mc.thePlayer.isCollidedHorizontally) {
            switch (mc.thePlayer.ticksExisted % 10) {
                case 8 -> {
                }
                case 1 -> {
                    event.setOnGround(true);
                    mc.thePlayer.motionY = 2 + (Math.random() * 0.2);
                    wasFlying = true;
                }
                case 9 -> {
                    mc.thePlayer.motionY = 0;
                }
            }
        } else {
            if (wasFlying) {
                mc.thePlayer.motionY = 0;
                wasFlying = false;
            }
        }
    }
}
