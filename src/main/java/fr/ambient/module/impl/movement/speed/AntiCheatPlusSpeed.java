package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class AntiCheatPlusSpeed extends ModuleMode {

    public AntiCheatPlusSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        if (mc.thePlayer.onGround && MoveUtil.moving()) {
            mc.thePlayer.motionY = 0.42;
            MoveUtil.strafe(MoveUtil.speed() * 1.5f);
        }

        if(mc.thePlayer.airTicks % 4 == 0 && mc.thePlayer.airTicks > 0){
            MoveUtil.strafe(2f);
        }else{
            MoveUtil.strafe(Math.min(MoveUtil.speed() * 1.05f, 0.5f));
        }
    }
}