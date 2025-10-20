package fr.ambient.module.impl.movement;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;

public class QuickStop extends Module {
    public QuickStop() {
        super(77, "Instantly stops your movement when you release the key.", ModuleCategory.MOVEMENT);
    }

    @SubscribeEvent
    public void onTickEvent(MovementEvent event){
        if(mc.thePlayer.hurtTime == 0 && mc.thePlayer.onGround && mc.thePlayer.movementInput.moveForward == 0f && mc.thePlayer.movementInput.moveStrafe == 0f){
            event.setX(0);
            event.setZ(0);
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        }
    }
}
