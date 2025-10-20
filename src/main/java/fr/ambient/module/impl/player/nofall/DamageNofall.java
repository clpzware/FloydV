package fr.ambient.module.impl.player.nofall;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class DamageNofall extends ModuleMode {



    public DamageNofall(String modeName, Module module) {
        super(modeName, module);
    }



    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.fallDistance > 2.5) {
            event.setOnGround(true);
            mc.thePlayer.fallDistance = 0;
        }
    }
}