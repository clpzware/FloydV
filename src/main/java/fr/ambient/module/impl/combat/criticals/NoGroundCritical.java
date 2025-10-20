package fr.ambient.module.impl.combat.criticals;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;



public class NoGroundCritical extends ModuleMode {
    public NoGroundCritical(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onPlayerNetworkTick(PreMotionEvent event) {
        event.setOnGround(false);
    }
}