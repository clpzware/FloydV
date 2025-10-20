package fr.ambient.util.player;

import com.google.common.base.Predicates;
import fr.ambient.Ambient;
import fr.ambient.util.InstanceAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;

import java.awt.*;
import java.util.List;


@UtilityClass
public class PlayerUtil implements InstanceAccess {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public int getSpeedEffect() {
        PotionEffect effect = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);

        if (effect == null) {
            return 0;
        }

        return effect.getAmplifier() + 1;
    }

    public double getLastDistance() {
        double dx = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
        double dz = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;

        return Math.sqrt(dx * dx + dz * dz);
    }

    public boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public boolean isBlockUnder(final double height, final boolean useBoundingBox) {
        if (height <= 0) {
            return false;
        }

        if (useBoundingBox) {
            final AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox().offset(0, -height, 0);

            return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty();
        } else {
            final int maxOffset = (int) Math.ceil(height);
            for (int offset = 0; offset < maxOffset; offset++) {
                if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - offset, mc.thePlayer.posZ)).getBlock().isFullBlock()) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isNearSlabAndStairs() {
        for (float y = -1; y < 1; y++) {
            if (isNearSlabAndStairs(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ))) {
                return true;
            }
        }
        return false;
    }

    public boolean isNearSlabAndStairs(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        return (block instanceof BlockStairs || block instanceof BlockSlab || block instanceof BlockFence || block instanceof BlockFenceGate);
    }


    public boolean Speedblacklist() {
        for (float y = -1; y < 1; y++) {
            if (isNonFullBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ))) {
                return true;
            }
        }
        return false;
    }

    public boolean isNonFullBlock(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockTallGrass || block instanceof BlockFlower || block instanceof BlockDoublePlant || block instanceof BlockReed
                || block instanceof BlockLilyPad || block instanceof BlockRailBase || block instanceof BlockTorch || block instanceof BlockCarpet || block instanceof BlockSnow;
    }




    public static float getEfficiency(final ItemStack itemStack, final Block block) {
        float getStrVsBlock = itemStack.getStrVsBlock(block);
        if (getStrVsBlock > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0) {
                getStrVsBlock += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        return getStrVsBlock;
    }

    public boolean isEntityTeamSameAsPlayer(EntityLivingBase target) {
        if (target.getTeam() != null && Minecraft.getMinecraft().thePlayer.getTeam() != null) {
            boolean ret0 = target.getDisplayName().getFormattedText().charAt(1) == Minecraft.getMinecraft().thePlayer.getDisplayName().getFormattedText().charAt(1);
            boolean ret1 = target.getTeam() == Minecraft.getMinecraft().thePlayer.getTeam();

            return ret0 || ret1;

        }
        return false;
    }

    public Vec3 getClosestPointToEntity(Entity e) {
        AxisAlignedBB bb = e.getEntityBoundingBox();

        double closestX = MathHelper.clamp_double(mc.thePlayer.posX, bb.minX, bb.maxX);
        double closestY = MathHelper.clamp_double(mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), bb.minY, bb.maxY);
        double closestZ = MathHelper.clamp_double(mc.thePlayer.posZ, bb.minZ, bb.maxZ);

        return new Vec3(closestX, closestY, closestZ);
    }

    public Vec3 getClosestPointToBoundingBox(AxisAlignedBB bb) {
        double closestX = MathHelper.clamp_double(mc.thePlayer.posX, bb.minX, bb.maxX);
        double closestY = MathHelper.clamp_double(mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), bb.minY, bb.maxY);
        double closestZ = MathHelper.clamp_double(mc.thePlayer.posZ, bb.minZ, bb.maxZ);
        return new Vec3(closestX, closestY, closestZ);
    }

    public double getDistanceToBoundingBox(AxisAlignedBB bb) {
        return new Vec3(mc.thePlayer).distanceTo(getClosestPointToBoundingBox(bb));
    }

    public Vec3 getClosestPointToEntity(Entity e, float marg) {
        AxisAlignedBB bb = e.getEntityBoundingBox().expand(marg, marg, marg);

        double closestX = MathHelper.clamp_double(mc.thePlayer.posX, bb.minX, bb.maxX);
        double closestY = MathHelper.clamp_double(mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), bb.minY, bb.maxY);
        double closestZ = MathHelper.clamp_double(mc.thePlayer.posZ, bb.minZ, bb.maxZ);

        return new Vec3(closestX, closestY, closestZ);
    }

    public Vec3 getPredictedPointOnEntity(Entity e, float marg) {
        AxisAlignedBB bb = e.getEntityBoundingBox().expand(marg, marg, marg);
        double closestX = MathHelper.clamp_double(mc.thePlayer.posX + (mc.thePlayer.motionX * 2), bb.minX, bb.maxX);
        double closestY = MathHelper.clamp_double(mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), bb.minY, bb.maxY);
        double closestZ = MathHelper.clamp_double(mc.thePlayer.posZ + (mc.thePlayer.motionZ * 2), bb.minZ, bb.maxZ);

        closestY -= (Math.abs(Math.sin(System.currentTimeMillis() / 1000) / 2));



        return new Vec3(closestX, closestY, closestZ);
    }

    public float getBiblicallyAccurateDistanceToEntity(Entity e) {
        return (float) getClosestPointToEntity(e).distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ));
    }

    public MovingObjectPosition getMouseMOP(float partialTicks, float distance, float yaw, float pitch) {
        return getMouseMOP(partialTicks, distance, mc.thePlayer.getVectorForRotation(pitch, yaw));
    }

    public MovingObjectPosition getMouseMOP(float partialTicks, float distance, Vec3 vec31) {
        Entity pointedEntity;
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        MovingObjectPosition movingObjectPosition = null;

        if (entity != null && mc.theWorld != null) {
            double d0 = mc.playerController.getBlockReachDistance();

            movingObjectPosition = entity.rayTraceCustomVector(distance, vec31);

            double d1 = d0;

            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            if (movingObjectPosition != null) {
                d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }

            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            pointedEntity = null;
            Vec3 vec33 = null;

            float f = 1.0F;

            List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
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

                        if (!flag1 && entity1 == entity.ridingEntity) {
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
            double maxDist = 3.0D;
            if (pointedEntity != null && vec3.distanceTo(vec33) > maxDist) {
                pointedEntity = null;
                movingObjectPosition = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || movingObjectPosition == null)) {
                movingObjectPosition = new MovingObjectPosition(pointedEntity, vec33);

                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    mc.pointedEntity = pointedEntity;
                }
            }
        }
        return movingObjectPosition;
    }

    public static Color getTeamColor(EntityLivingBase target) {
        if (target.getDisplayName().getFormattedText().charAt(0) == '§') {
            char targeted = (target.getDisplayName().getFormattedText().charAt(1));
            for (EnumChatFormatting enumChatFormatting : EnumChatFormatting.values()) {
                if (String.valueOf(enumChatFormatting.formattingCode).equals(String.valueOf(targeted))) {
                    return enumChatFormatting.color;
                }
            }
        }
        return Ambient.getInstance().getHud().getCurrentTheme().color2;
    }
}
