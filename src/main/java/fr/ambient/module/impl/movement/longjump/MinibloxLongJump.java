package fr.ambient.module.impl.movement.longjump;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.render.styles.ModernStyle;

public class MinibloxLongJump extends ModuleMode {

    private int glideTicks = 0;

    public MinibloxLongJump(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        glideTicks = 0;
    }



    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        ModernStyle.drawProgress((float) glideTicks / 32);
    }

    @SubscribeEvent
    public void onTick(UpdateEvent event) {
        if (glideTicks < 32) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 1;
            }
            if (mc.thePlayer.fallDistance > 0.1) {
                if (glideTicks == 15) {
                    mc.thePlayer.motionY = 0.5;
                } else {
                    mc.thePlayer.motionY = -0.0980000019073;
                }
                MoveUtil.strafe(0.5);
            }
            glideTicks++;
        } else {
            Ambient.getInstance().getModuleManager().getModule(LongJump.class).setEnabled(false);
        }
    }
}
