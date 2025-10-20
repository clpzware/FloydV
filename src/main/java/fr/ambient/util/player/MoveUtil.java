package fr.ambient.util.player;

import fr.ambient.Ambient;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.util.InstanceAccess;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;

public class MoveUtil implements InstanceAccess {


    public static double getVerusLimit(boolean dif) {
        ///This is made & given by nightfull don't ask idk man im not doing all that myself
        if (dif && mc.thePlayer.fallDistance > 0.2) {
            return MoveUtil.getBaseMoveSpeed();
        }

        if (mc.thePlayer.fallDistance < 0.2) {
            if (mc.thePlayer.isSprinting()) {
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
                        if (effect.getPotionID() == 1) {
                            return mc.thePlayer.onGround ? (effect.getAmplifier() == 1 ? 0.7f : 0.62f) : (effect.getAmplifier() == 1 ? 0.81f : 0.62f);
                        }
                    }
                }
                return mc.thePlayer.onGround ? 0.54f : 0.46f;
            }
            return MoveUtil.getBaseMoveSpeed() * 1.02f;
        }

        return 0;
    }



    public static double getBaseMoveSpeed() {
        final double speed = 0.2873;

        if (!mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return speed;
        }

        int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed) != null ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() : 0;

        return speed * (1.0 + 0.2 * (amplifier + 1));
    }

    public static boolean moving() {
        return mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0;
    }
    public static boolean isDiag(){
        return Math.abs(mc.thePlayer.motionX) > 0.07 && Math.abs(mc.thePlayer.motionZ) > 0.07;
    }

    public static double speed() {
        return Math.hypot(Math.abs(mc.thePlayer.motionX), Math.abs(mc.thePlayer.motionZ));
    }

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F)
            forward = -0.5F;
        else if (moveForward > 0F)
            forward = 0.5F;

        if (moveStrafing > 0F)
            rotationYaw -= 90F * forward;
        if (moveStrafing < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }
    public static double getDirection() {
        float moveForward = mc.thePlayer.moveForward;
        float moveStrafing = mc.thePlayer.moveStrafing;
        float rotationYaw = mc.thePlayer.movementYaw;


        boolean isGoingBackward = moveForward < 0F;
        boolean isGoingForward = moveForward > 0F;
        float forward = 1F;
        if (isGoingBackward) {
            rotationYaw += 180F;
        }
        if (isGoingBackward) forward = -0.5F;
        else if (isGoingForward) forward = 0.5F;
        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;
        return Math.toRadians(rotationYaw);
    }

    public static void fixMovement(final MoveInputEvent event, final float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();

        final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(Ambient.getInstance().getRotationComponent().rotationYaw, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float fw = 0, sw = 0, diff = Float.MAX_VALUE;

        float[] values = new float[]{-1f, 0f, 1f};
        
        for (float fwValues : values) {
            for (float strafeValues : values) {
                if (strafeValues == 0 && fwValues == 0) continue;

                final double diffAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(yaw, fwValues, strafeValues)));
                final double difference = Math.abs(angle - diffAngle);

                if (difference < diff) {
                    diff = (float) difference;
                    fw = fwValues;
                    sw = strafeValues;
                }
            }
        }

        event.setForward(fw);
        event.setStrafe(sw);
    }


    public static double getSwiftnessSpeed(final double speed, final double swiftnessMultiplier) {
        PotionEffect effect = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);

        if (effect == null) {
            return speed;
        }

        return speed * (1 + swiftnessMultiplier * (effect.getAmplifier() + 1));
    }



    public static void strafe(float v, float v1, float v2) {
        float speedValue = 0;

        if(mc.thePlayer.isPotionActive(Potion.moveSpeed)){
            if (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0) {
                speedValue = v1;
            } else {
                speedValue = v2;
            }
        }else{
            speedValue = v;
        }
        strafe(speedValue);
    }

    public static void strafe(final double speed) {
        if (!moving())
            return;

        double yaw = getDirection();

        mc.thePlayer.motionX = -MathHelper.sin((float) yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos((float) yaw) * speed;
    }

    public static void strafe2(final double speed, float v) {
        strafe(speed);
    }
    public static void strafeWithEvent(MovementEvent event, float speed) {
        double yaw = getDirection();

        event.setX(-MathHelper.sin((float) yaw) * speed);
        event.setZ(MathHelper.cos((float) yaw) * speed);
    }
    public static double getSpeed(Entity entity) {
        return Math.hypot(Math.abs(entity.posX - entity.lastTickPosX), Math.abs(entity.posZ - entity.lastTickPosZ)) * mc.timer.timerSpeed * 20;
    }
}