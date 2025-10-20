package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

public class KoderaSlowFly extends ModuleMode {
    public KoderaSlowFly(String modeName, Module module) {
        super(modeName, module);
    }
    @SubscribeEvent
    private void onPre(PreMotionEvent event){
        if(mc.thePlayer.onGround ||mc.thePlayer.posY % 1 == 0){
            event.setOnGround(true);
        }
    }
    @SubscribeEvent
    private void onMove(MovementEvent event){
        if(mc.thePlayer.posY % 1 == 0){
            event.setY(0);
        }
    }
}
