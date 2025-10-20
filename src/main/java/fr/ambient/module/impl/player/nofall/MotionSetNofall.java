package fr.ambient.module.impl.player.nofall;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.NoFall;

public class MotionSetNofall extends ModuleMode {

    private final NoFall noFall = (NoFall) this.getSuperModule();

    public MotionSetNofall(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.fallDistance > 3 &&  noFall.getDistanceToGround() != 0) {
            event.setOnGround(true);
            mc.thePlayer.fallDistance = 0f;
            mc.thePlayer.motionY = 0;
        }
    }
}