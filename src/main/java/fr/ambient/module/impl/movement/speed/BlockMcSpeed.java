package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;

public class BlockMcSpeed extends ModuleMode {

    public BlockMcSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        if (mc.thePlayer.onGround & MoveUtil.moving()) {
            mc.thePlayer.motionY = 0.42;
            MoveUtil.strafe(0.48f, 0.48f, 0.7f);
        }
        MoveUtil.strafe(MoveUtil.speed());
        if (mc.thePlayer.airTicks == 4) {
            mc.thePlayer.motionY = -0.0980000019073;
        }
    }

    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        event.setJumping(false);
    }
}