package fr.ambient.module.impl.player.scaffold.tower;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.PlayerUtil;

import static fr.ambient.util.player.MoveUtil.getSwiftnessSpeed;
import static fr.ambient.util.player.MoveUtil.moving;

public class WatchdogFastTower extends ModuleMode {

    public WatchdogFastTower(String modeName, Module module) {
        super(modeName, module);
    }


    public boolean canTower = false;
    public boolean istowering = false;
    private int tickCounter;

    public void onEnable() {
        canTower = false;
        istowering = false;
        tickCounter = 0;
    }

    public void onDisable() {
        istowering = false;
    }

    public boolean canTower() {
        return mc.gameSettings.keyBindJump.isKeyDown() && (moving() || tickCounter != 20) && !mc.thePlayer.isCollidedHorizontally && !PlayerUtil.isNearSlabAndStairs();
    }

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {
        if (canTower()) {
            if (mc.thePlayer.onGround && mc.thePlayer.posY % 1 == 0) {
                istowering = true;
            }
            if (istowering) {
                tickCounter++;
                switch ((int) Math.round(mc.thePlayer.posY % 1 * 100)) {
                    case 0 -> {
                        mc.thePlayer.motionY = 0.42f;
                        MoveUtil.strafe(getSwiftnessSpeed(0.27, 0.1));
                    }
                    case 42 -> {
                        mc.thePlayer.motionY = 0.33f;
                        MoveUtil.strafe(getSwiftnessSpeed(0.27, 0.1));
                    }
                    case 75 -> mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                }
            }
        } else {
            istowering = false;
            tickCounter = 0;
        }
    }
}