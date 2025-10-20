package fr.ambient.module.impl.movement.speed;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

import static fr.ambient.util.player.MoveUtil.getSwiftnessSpeed;

public class WatchdogGroundSpeed extends ModuleMode {
    public WatchdogGroundSpeed(String modeName, Module module) {
        super(modeName, module);
    }


    public void onEnable() {
        if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ);
        }
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.onGround && MoveUtil.moving() && !mc.gameSettings.keyBindJump.isPressed() && mc.thePlayer.hurtTime == 0) {
            event.setPosY(event.getPosY() + 1.0E-6);
            MoveUtil.strafe(getSwiftnessSpeed(0.2f, 0.2));
        }
    }
}