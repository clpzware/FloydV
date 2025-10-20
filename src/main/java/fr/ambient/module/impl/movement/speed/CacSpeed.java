package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;

public class CacSpeed extends ModuleMode {

    public CacSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(UpdateEvent event) {
        if (mc.thePlayer.onGround && MoveUtil.moving()) {
            mc.thePlayer.motionY = 0.42;
            ChatUtil.display(MoveUtil.speed());
            MoveUtil.strafe(Math.min(MoveUtil.speed(), 0.53f));
        }
        if (mc.thePlayer.airTicks < 3) {
            mc.thePlayer.motionY -= 0.3f;
        }
        MoveUtil.strafe(MoveUtil.speed());
    }

    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        event.setJumping(false);
    }
}