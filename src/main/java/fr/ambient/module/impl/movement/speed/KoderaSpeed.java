package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class KoderaSpeed extends ModuleMode {
    public KoderaSpeed(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onMoveInput(MoveInputEvent event){
        if(MoveUtil.moving()){
            event.setJumping(true);
        }
    }


    @SubscribeEvent
    private void onMove(PreMotionEvent event){
        if(mc.thePlayer.movementInput.moveForward > 0){
            MoveUtil.strafe(MoveUtil.speed());
        }

        if(mc.thePlayer.hurtTime > 0){

            double debugValue = 0.7f;

            MoveUtil.strafe((float) debugValue, (float) (debugValue * 1.2f), (float) (debugValue * 1.4f));
        }
    }



}
