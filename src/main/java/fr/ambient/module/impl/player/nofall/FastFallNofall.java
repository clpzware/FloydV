package fr.ambient.module.impl.player.nofall;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class FastFallNofall extends ModuleMode {


    public FastFallNofall(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.fallDistance > 3 && mc.thePlayer.fallDistance <= 30 && mc.thePlayer.hurtTime == 0) {
            mc.thePlayer.motionY = -9f;
            event.setOnGround(true);
        }
    }
}