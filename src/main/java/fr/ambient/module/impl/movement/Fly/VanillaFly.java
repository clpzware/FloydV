package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Flight;
import fr.ambient.util.player.MoveUtil;

public class VanillaFly extends ModuleMode {

    private final Flight fly = (Flight) this.getSuperModule();

    public VanillaFly(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    @SubscribeEvent
    public void onTick(UpdateEvent ignoredEvent) {
        mc.thePlayer.motionY = mc.gameSettings.keyBindJump.pressed ? fly.Speed.getValue() * 0.6 : mc.gameSettings.keyBindSneak.pressed ? -fly.Speed.getValue() * 0.6 : 0;
        if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
            MoveUtil.strafe(fly.Speed.getValue());
        } else {
            MoveUtil.strafe(0);
        }
    }
}
