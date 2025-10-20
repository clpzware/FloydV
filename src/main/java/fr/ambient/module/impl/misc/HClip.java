package fr.ambient.module.impl.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;

public class HClip extends Module {
    public HClip() {
        super(111, ModuleCategory.MISC);
    }


    public void onEnable(){

    }

    @SubscribeEvent
    private void onMoveEvent(MovementEvent event){
        double speed = mc.thePlayer.capabilities.getWalkSpeed() * 2.806;
        double rad = mc.thePlayer.rotationYaw * Math.PI / 180;

        double x = -Math.sin(rad) * speed;
        double z = Math.cos(rad) * speed;




        mc.thePlayer.setVelocity(x, mc.thePlayer.motionY, z);
    }

}
