package fr.ambient.util.player;

import com.google.common.base.Predicates;
import fr.ambient.util.InstanceAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.List;

@UtilityClass
public class RotationUtil implements InstanceAccess {
    private static final double RAD_TO_DEG = 180 / Math.PI;
    private static final double RANDOM_SENSITIVITY_VARIANCE = 1;
    private static final double SENSITIVITY_MULTIPLIER = 0.6f;
    private static final double SENSITIVITY_BASE = 0.2f;
    private static final double SENSITIVITY_EXPONENT = 8.0f * 0.15;
    private static final float EXTENDED_REACH_DISTANCE = 3.0F;
    private static final float COLLISION_BORDER_SIZE = 1.0F;

    public float[] applySensitivity(final float[] rotation, final float[] previousRotation) {
        float mouseSensitivity = (mc.gameSettings.mouseSensitivity * 0.6F) + 0.2F;
        float gcd = mouseSensitivity * mouseSensitivity * mouseSensitivity * 1.2f;

        float deltaYaw = rotation[0] - previousRotation[0];
        float deltaPitch = rotation[1] - previousRotation[1];

        float yaw = deltaYaw - (deltaYaw % gcd);
        float pitch = deltaPitch - (deltaPitch % gcd);

        return new float[] {
                previousRotation[0] + yaw,
                MathHelper.clamp_float(previousRotation[1] + pitch, -90, 90)
        };
    }

    public float[] reset(final float[] rotation) {
        if (rotation == null) return null;

        float yaw = rotation[0] + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation[0]),
                pitch = mc.thePlayer.rotationPitch;

        return new float[] { yaw, pitch };
    }

    public float[] getRotationsVector(Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double distance = Math.sqrt(delta.xCoord * delta.xCoord + delta.zCoord * delta.zCoord);
        float yaw = (float) Math.toDegrees(Math.atan2(delta.zCoord, delta.xCoord)) - 90.0F,
                pitch = (float) -Math.toDegrees(Math.atan2(delta.yCoord, distance));

        return new float[]{yaw, pitch};
    }

    public float[] smooth(final float[] lastRotation, final float[] targetRotation, final double speed) {
        double deltaYaw = MathHelper.wrapAngleTo180_double(targetRotation[0] - lastRotation[0]),
                deltaPitch = targetRotation[1] - lastRotation[1];
        double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);

        double maxYaw = speed * Math.abs(deltaYaw / distance),
                maxPitch = speed * Math.abs(deltaPitch / distance);

        float yaw = lastRotation[0] + (float) Math.max(Math.min(deltaYaw, maxYaw), -maxYaw),
                pitch = lastRotation[1] + (float) Math.max(Math.min(deltaPitch, maxPitch), -maxPitch);

        return applySensitivity(new float[] { yaw, pitch }, lastRotation);
    }

    public static Vec3 getBestLookVector(Vec3 look, AxisAlignedBB aabb, double expand) {
        aabb = aabb.expand(expand, expand, expand);
        return new Vec3(
                Math.max(aabb.minX, Math.min(aabb.maxX, look.xCoord)),
                Math.max(aabb.minY, Math.min(aabb.maxY, look.yCoord)),
                Math.max(aabb.minZ, Math.min(aabb.maxZ, look.zCoord))
        );
    }

    public static double getDistanceToEntity(Entity entity) {
        if (mc.thePlayer == null || entity == null) return 0;
        return mc.thePlayer.getPositionEyes(1.0f).distanceTo(getBestLookVector(mc.thePlayer.getPositionEyes(1F), entity.getEntityBoundingBox(), 0.0));
    }

    public static float[] getRotation(Entity entity) {
        final double differenceX = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        final double differenceY = entity.posY + Minecraft.getMinecraft().thePlayer.posY;
        final double differenceZ = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        final float rotationYaw = (float) Math.toDegrees(Math.atan2(differenceZ, differenceX)) - 90.0f;
        final float rotationPitch = (float) Math.toDegrees(Math.atan2(differenceY, Math.hypot(differenceX, differenceZ)));
        final float finishedYaw = Minecraft.getMinecraft().thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(rotationYaw - Minecraft.getMinecraft().thePlayer.rotationYaw);
        final float finishedPitch = -MathHelper.clamp_float(Minecraft.getMinecraft().thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(rotationPitch - Minecraft.getMinecraft().thePlayer.rotationPitch), -90, 90);
        return new float[]{finishedYaw, finishedPitch};
    }

    public float[] getRotationDifference(Vec3 playerPos, Vec3 entityPos, float currentYaw, float currentPitch) {
        double entityX = entityPos.xCoord, entityY = entityPos.yCoord, entityZ = entityPos.zCoord;
        double playerX = playerPos.xCoord, playerY = playerPos.yCoord, playerZ = playerPos.zCoord;
        double diffX = entityX - playerX, diffY = entityY - playerY, diffZ = entityZ - playerZ;
        double calc = Math.atan2(diffX, diffZ) * (180d / Math.PI);
        double angle = currentYaw - MathHelper.wrapAngleTo180_double((currentYaw + calc) % 360);
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);
        float Diff1 = currentPitch - pitch;
        float Diff2 = (float) (currentYaw - angle);
        return new float[]{Diff2, Diff1};
    }

    public MovingObjectPosition getMouseEntity(float partialTicks, float distance, float[] rotations) {
        Vec3 vec31 = mc.thePlayer.getVectorForRotation(rotations[1], rotations[0]);
        MovingObjectPosition objectMouseOver;
        Entity pointedEntity;
        objectMouseOver = mc.thePlayer.rayTraceCustom(distance, rotations[0], rotations[1]);
        double d1 = distance;
        Vec3 vec3 = new Vec3(mc.thePlayer);
        boolean flag = false;
        int i = 3;
        if (objectMouseOver != null) {
            d1 = objectMouseOver.hitVec.distanceTo(vec3);
        }
        Vec3 vec32 = vec3.addVector(vec31.xCoord * (double) distance, vec31.yCoord * (double) distance, vec31.zCoord * (double) distance);
        pointedEntity = null;
        Vec3 vec33 = null;
        float f = 1.0F;
        List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().addCoord(vec31.xCoord * (double) distance, vec31.yCoord * (double) distance, vec31.zCoord * (double) distance).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
        double d2 = d1;

        for (Entity entity1 : list) {
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

            if (axisalignedbb.isVecInside(vec3)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity1;
                    vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                    d2 = 0.0D;
                }
            } else if (movingobjectposition != null) {
                double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                if (d3 < d2 || d2 == 0.0D) {
                    boolean flag1 = false;

                    if (!flag1 && entity1 == mc.thePlayer.ridingEntity) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                        }
                    } else {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }
        }
        if (pointedEntity != null && vec3.distanceTo(vec33) > (double) distance) {
            pointedEntity = null;
            objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
        }

        if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
        }

        return objectMouseOver;
    }
}