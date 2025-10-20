package fr.ambient.module.impl.movement.longjump;

import fr.ambient.Ambient;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.util.player.MoveUtil;

public class GhostlyLongjump extends ModuleMode {

    private int ticks = 0;
    private double last = 0.0;

    public GhostlyLongjump(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        BlinkComponent.onDisable();
        ticks = 0;
        last = 0.0;
    }

    @Override
    public void onEnable() {
        ticks = 0;
        last = 0.0;
    }

    @SubscribeEvent
    public void onTick(UpdateEvent event) {
        LongJump lj = (LongJump) this.getSuperModule();
        if (mc.thePlayer.ticksExisted % 5 == 0) {
            BlinkComponent.onEnable();
        } else {
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                BlinkComponent.onDisable();
            }
        }

        if (!mc.thePlayer.onGround) {
            ticks++;
        } else {
            mc.thePlayer.jump();
        }

        if (ticks % 70 == 0) {
            last = mc.thePlayer.motionY;
            if (lj.boost.getValue()) {
                MoveUtil.strafe(lj.Speed.getValue());
            }
        }

        mc.thePlayer.motionY = last;

        if (mc.thePlayer.motionY < 0.1) {
            Ambient.getInstance().getModuleManager().getModule(LongJump.class).setEnabled(false);
        }
    }
}
