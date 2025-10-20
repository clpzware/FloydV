package fr.ambient.module.impl.movement.longjump;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.util.player.MoveUtil;

public class VanillaLongjump extends ModuleMode {
    public VanillaLongjump(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    @SubscribeEvent
    public void onTick(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = ((LongJump)this.getSuperModule()).height.getValue();
        }
        MoveUtil.strafe(((LongJump)this.getSuperModule()).Speed.getValue());
    }
}