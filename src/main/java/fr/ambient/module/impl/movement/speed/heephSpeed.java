package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class heephSpeed extends ModuleMode {


    public heephSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    public void onEnable() {
        mc.thePlayer.setPosition(mc.thePlayer.posX - 0.0000000000000001, mc.thePlayer.posY - 0.001, mc.thePlayer.posZ - 0.0000000000000001);
    }


    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        if (MoveUtil.moving()) {
            mc.thePlayer.setPosition(mc.thePlayer.posX - 0.0000000000000001, mc.thePlayer.posY + 0.0001, mc.thePlayer.posZ - 0.0000000000000001);
            event.setOnGround(true);
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.0000001f;
                mc.timer.timerSpeed = 0.9f;

            }
        } else {
            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
        }

        MoveUtil.strafe(1.1f, 0.9f, 0.94f);
    }
}