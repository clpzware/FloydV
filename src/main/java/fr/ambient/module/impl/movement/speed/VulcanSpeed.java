package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;

public class VulcanSpeed extends ModuleMode {

    public VulcanSpeed(String modeName, Module module) {
        super(modeName, module);
    }



    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        Speed speedModule = (Speed) this.getSuperModule();
        switch (speedModule.vulcanmode.getValue()) {
            case "Old Ground" -> {
                if (mc.gameSettings.keyBindJump.isKeyDown()) return;
                if (mc.thePlayer != null && MoveUtil.moving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.005;
                        event.setPosY(event.getPosY() + (Math.random() * 0.1));
                        MoveUtil.strafe(0.4f, 0.6f, 0.55f);
                    }
                    MoveUtil.strafe(MoveUtil.speed());
                }
            }
            case "Old Strafe" -> {
                if (mc.thePlayer != null && MoveUtil.moving() && !mc.gameSettings.keyBindJump.isPressed()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.42;
                        MoveUtil.strafe(0.445f, 0.475f, 0.75f);
                    }
                    switch (mc.thePlayer.airTicks) {
                        case 5, 8, 9 -> {
                            event.setOnGround(true);
                            event.setPosY(event.getPosY() - 0.3);
                        }
                    }
                    if (mc.thePlayer.hurtTime != 0 && mc.thePlayer.airTicks > 4) {
                        MoveUtil.strafe(MoveUtil.speed() * ((Speed) this.getSuperModule()).damageboost.getValue());
                    }
                    MoveUtil.strafe(MoveUtil.speed());
                }
            }
            case "Old Glide" -> {
                if (mc.thePlayer.onGround && MoveUtil.moving()) {
                    mc.thePlayer.motionY = 0.42;
                    MoveUtil.strafe(0.48f, 0.48f, 0.7f);
                }

                if (mc.thePlayer.fallDistance > 0.1) {
                    mc.thePlayer.motionY = (mc.thePlayer.ticksExisted % 3 == 0) ? -0.155 : -0.1;
                }
            }
            case "2.9.2.4+" -> {
                if (!MoveUtil.moving()) return;
                if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down(1)).getBlock() instanceof BlockAir) return;
                if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 3 == 0) {
                    mc.thePlayer.motionY = 0.42;
                    if (mc.thePlayer.moveStrafing == 0) {
                        MoveUtil.strafe(0.4f, 0.45f, 0.51f);
                    } else {
                        MoveUtil.strafe(0.29f, 0.45f, 0.45f);
                    }
                } else {
                    if (mc.thePlayer.fallDistance <= 1) {
                        mc.thePlayer.motionY = -1;
                    } else {
                        if (mc.thePlayer.airTicks == 3)
                            mc.thePlayer.motionY = -0.098000019073;
                    }
                }
                MoveUtil.strafe(MoveUtil.speed());
            }
        }
    }

    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        Speed speedModule = (Speed) this.getSuperModule();
        if (MoveUtil.moving() && !speedModule.vulcanmode.is("Old Ground")) {
            event.setJumping(false);
        }
    }
}