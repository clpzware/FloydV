package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class CreativeFly extends ModuleMode {

    public CreativeFly(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        mc.thePlayer.capabilities.isFlying = false;
        mc.thePlayer.capabilities.allowFlying = false;
    }

    @SubscribeEvent
    public void onTick(UpdateEvent ignoredEvent) {
        mc.thePlayer.capabilities.allowFlying = true;
    }
}
