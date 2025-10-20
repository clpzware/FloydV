package fr.ambient.module.impl.player.nofall;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class NoGroundNofall extends ModuleMode {
    public NoGroundNofall(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        event.setOnGround(false);
    }
}