package fr.ambient.module.impl.movement.longjump;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.util.player.MoveUtil;

public class VulcanLongJump extends ModuleMode {

    private int ticks = 0;

    public VulcanLongJump(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    public void onEnable() {
        ticks = 0;
    }

    @SubscribeEvent
    public void onTick(UpdateEvent event) {
        ticks++;

        switch (ticks) {
            case 1 -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 3.5, mc.thePlayer.posZ);
                }
            }
            case 5,6,7 -> mc.thePlayer.motionY = 4;
            case 14 -> MoveUtil.strafe(0.32f);
            case 16 -> {
                mc.thePlayer.motionY = 0;
                MoveUtil.strafe(0.32f);
            }
        }
        if (ticks > 16) {
            if (mc.thePlayer.onGround) {
                Ambient.getInstance().getModuleManager().getModule(LongJump.class).setEnabled(false);
            }
            if (mc.thePlayer.motionY < 0) {
                if (mc.thePlayer.ticksExisted % 12 == 0) {
                    mc.thePlayer.motionY = -0.0980000019073;
                }
            }
        }
    }
}