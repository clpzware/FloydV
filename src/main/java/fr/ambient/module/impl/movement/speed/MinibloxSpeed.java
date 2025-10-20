package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.player.MoveUtil;

public class MinibloxSpeed extends ModuleMode {

    private int glideTicks = 0;
    private boolean hastouchedground = true;

    public MinibloxSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        if (mc.thePlayer != null && MoveUtil.moving() && !mc.gameSettings.keyBindJump.isPressed()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.4f;
                MoveUtil.strafe(0.33);
            } else {
                MoveUtil.strafe(MoveUtil.speed() * 1.05);
            }

            if (((Speed) this.getSuperModule()).fastfall.getValue()) {
                mc.thePlayer.motionY += switch (mc.thePlayer.airTicks) {
                    case 3 -> -0.13;
                    case 4 -> -0.5;
                    default -> 0;
                };
            }
        }

        MoveUtil.strafe(MoveUtil.speed() * 1.01);

        if (mc.thePlayer.onGround) {
            glideTicks = 0;
        } else {
            glideTicks++;
        }


        if (((Speed) this.getSuperModule()).minibloxglide.getValue()) {
            if (mc.thePlayer.onGround) {
                hastouchedground = true;
            }

            if (hastouchedground && glideTicks <= 22 && mc.thePlayer.fallDistance > 0.1) {
                mc.thePlayer.motionY = -0.0980000019073;
                MoveUtil.strafe(0.40);

                if (glideTicks == 22) {
                    hastouchedground = false;
                }
            }
        }
    }


    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        if (MoveUtil.moving()) {
            event.setJumping(false);
        }
    }
}
