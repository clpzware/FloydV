package fr.ambient.util;

import fr.ambient.Ambient;
import fr.ambient.util.player.ChatUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

import java.util.Arrays;
import java.util.List;


@UtilityClass
public class BlockUtil {
    public static final List<Block> blockBlacklist = Arrays.asList(
            Blocks.air, Blocks.water, Blocks.tnt, Blocks.chest, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava,
            Blocks.tnt, Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars,
            Blocks.snow_layer,Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore,
            Blocks.chest, Blocks.torch, Blocks.anvil, Blocks.trapped_chest, Blocks.noteblock, Blocks.jukebox, Blocks.tnt,
            Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.sand, Blocks.lit_redstone_ore, Blocks.quartz_ore,
            Blocks.redstone_ore, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate,
            Blocks.heavy_weighted_pressure_plate, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.enchanting_table,
            Blocks.red_flower, Blocks.double_plant, Blocks.yellow_flower, Blocks.web, Blocks.tripwire_hook, Blocks.ender_chest,
            Blocks.furnace, Blocks.crafting_table, Blocks.dispenser, Blocks.dropper, Blocks.jukebox, Blocks.hopper, Blocks.trapdoor,Blocks.pumpkin, Blocks.ladder
    );

    Minecraft mc = Minecraft.getMinecraft();

    public PosFace getBlockFacing(BlockPos pos) {
        
        Minecraft mc = Minecraft.getMinecraft();
        BlockPos currentPos;
        EnumFacing currentFacing;


        if (mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, -1, 0);
            currentFacing = EnumFacing.UP;
        } else if (mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(-1, 0, 0);
            currentFacing = EnumFacing.EAST;
        } else if (mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(1, 0, 0);
            currentFacing = EnumFacing.WEST;
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, 0, -1);
            currentFacing = EnumFacing.SOUTH;
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, 0, 1);
            currentFacing = EnumFacing.NORTH;
        } else if (mc.theWorld.getBlockState(pos.add(-1, 0, -1)).getBlock() != Blocks.air) {
            currentFacing = EnumFacing.EAST;
            currentPos = pos.add(-1, 0, -1);
        } else if (mc.theWorld.getBlockState(pos.add(1, 0, 1)).getBlock() != Blocks.air) {
            currentFacing = EnumFacing.WEST;
            currentPos = pos.add(1, 0, 1);
        } else if (mc.theWorld.getBlockState(pos.add(1, 0, -1)).getBlock() != Blocks.air) {
            currentFacing = EnumFacing.SOUTH;
            currentPos = pos.add(1, 0, -1);
        } else if (mc.theWorld.getBlockState(pos.add(-1, 0, 1)).getBlock() != Blocks.air) {
            currentFacing = EnumFacing.NORTH;
            currentPos = pos.add(-1, 0, 1);
        } else if (mc.theWorld.getBlockState(pos.add(0, -1, 1)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, -1, 1);
            currentFacing = EnumFacing.UP;
        } else if (mc.theWorld.getBlockState(pos.add(0, -1, -1)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, -1, -1);
            currentFacing = EnumFacing.UP;
        } else if (mc.theWorld.getBlockState(pos.add(1, -1, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(1, -1, 0);
            currentFacing = EnumFacing.UP;
        } else if (mc.theWorld.getBlockState(pos.add(-1, -1, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(-1, -1, 0);
            currentFacing = EnumFacing.UP;
        } else if (mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, -1, 0);
            currentFacing = EnumFacing.UP;
        } else if (mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(-1, 0, 0);
            currentFacing = EnumFacing.EAST;
        } else if (mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() != Blocks.air) {
            currentPos = pos.add(1, 0, 0);
            currentFacing = EnumFacing.WEST;
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, 0, -1);
            currentFacing = EnumFacing.SOUTH;
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() != Blocks.air) {
            currentPos = pos.add(0, 0, 1);
            currentFacing = EnumFacing.NORTH;
        } else {
            currentPos = null;
            currentFacing = null;
        }


        return new PosFace(currentPos, currentFacing);
    }

    public float[] getRotationToBlockDirect(PosFace pf) {
        Minecraft mc = Minecraft.getMinecraft();
        double offx = 0, offy = 0, offz = 0;


        switch (pf.facing) {
            case NORTH:
                offz = -0.5f;
                break;
            case SOUTH:
                offz = 0.5f;
                break;
            case EAST:
                offx = 0.5f;
                break;
            case WEST:
                offx = -0.5f;
                break;
            case UP:
                offy = 0.5f;
                break;
            case DOWN:
                offy = -0.5f;
                break;
        }


        double deltaX = pf.getPos().getX() + 0.5 + offx - mc.thePlayer.posX,
                deltaY = pf.getPos().getY() + 0.5 + offy - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight()),
                deltaZ = pf.getPos().getZ() + 0.5 + offz - mc.thePlayer.posZ,
                distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));

        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ)),
                pitch = (float) -Math.toDegrees(Math.atan(deltaY / distance));

        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if (deltaX < 0 && deltaZ < 0) {
            yaw = (float) (90 + v);
        } else if (deltaX > 0 && deltaZ < 0) {
            yaw = (float) (-90 + v);
        }
        return new float[]{yaw, pitch};
    }

    public float[] getRotationToBlockDirectWithBestPos(PosFace pf) {
        Minecraft mc = Minecraft.getMinecraft();
        double offx = 0, offy = 0, offz = 0;


        switch (pf.facing) {
            case NORTH:
                offz = -0.5f;
                break;
            case SOUTH:
                offz = 0.5f;
                break;
            case EAST:
                offx = 0.5f;
                break;
            case WEST:
                offx = -0.5f;
                break;
            case UP:
                offy = 0.5f;
                break;
            case DOWN:
                offy = -0.5f;
                break;
        }


        double deltaX = pf.getPos().getX() + 0.5 + offx - mc.thePlayer.posX,
                deltaY = pf.getPos().getY() + 0.5 + offy - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight()),
                deltaZ = pf.getPos().getZ() + 0.5 + offz - mc.thePlayer.posZ,
                distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));


        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ)),
                pitch = (float) -Math.toDegrees(Math.atan(deltaY / distance));

        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if (deltaX < 0 && deltaZ < 0) {
            yaw = (float) (90 + v);
        } else if (deltaX > 0 && deltaZ < 0) {
            yaw = (float) (-90 + v);
        }
        return new float[]{yaw, pitch};
    }

    public float[] getRotationToBlockEdgeFromEyePos(PosFace pf, Vec3 eyePos) {
        Minecraft mc = Minecraft.getMinecraft();
        double offx = 0, offy = 0, offz = 0;

        switch (pf.facing) {
            case NORTH:
                offz = -0.5;
                offx = (mc.thePlayer.posX > pf.getPos().getX() + 0.5) ? -0.5 : 0.5;
                break;
            case SOUTH:
                offz = 0.5;
                offx = (mc.thePlayer.posX > pf.getPos().getX() + 0.5) ? -0.5 : 0.5;
                break;
            case EAST:
                offx = 0.5;
                offz = (mc.thePlayer.posZ > pf.getPos().getZ() + 0.5) ? -0.5 : 0.5;
                break;
            case WEST:
                offx = -0.5;
                offz = (mc.thePlayer.posZ > pf.getPos().getZ() + 0.5) ? -0.5 : 0.5;
                break;
            case UP:
                offy = 0.5;
                break;
            case DOWN:
                offy = -0.5;
                break;
        }

        offx *= 0.999;
        offz *= 0.999;

        double deltaX = pf.getPos().getX() + 0.5 + offx - eyePos.xCoord,
                deltaY = pf.getPos().getY() + 0.5 + offy - eyePos.yCoord,
                deltaZ = pf.getPos().getZ() + 0.5 + offz - eyePos.zCoord,
                distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));


        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ)),
                pitch = (float) -Math.toDegrees(Math.atan(deltaY / distance));

        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if (deltaX < 0 && deltaZ < 0) {
            yaw = (float) (90 + v);
        } else if (deltaX > 0 && deltaZ < 0) {
            yaw = (float) (-90 + v);
        }
        return new float[]{yaw, pitch};
    }

    public float[] getRotationToBlockEdge(PosFace pf) {
        Minecraft mc = Minecraft.getMinecraft();
        double offx = 0, offy = 0, offz = 0;

        switch (pf.facing) {
            case NORTH:
                offz = -0.5;
                offx = (mc.thePlayer.posX > pf.getPos().getX() + 0.5) ? -0.5 : 0.5;
                break;
            case SOUTH:
                offz = 0.5;
                offx = (mc.thePlayer.posX > pf.getPos().getX() + 0.5) ? -0.5 : 0.5;
                break;
            case EAST:
                offx = 0.5;
                offz = (mc.thePlayer.posZ > pf.getPos().getZ() + 0.5) ? -0.5 : 0.5;
                break;
            case WEST:
                offx = -0.5;
                offz = (mc.thePlayer.posZ > pf.getPos().getZ() + 0.5) ? -0.5 : 0.5;
                break;
            case UP:
                offy = 0.5;
                break;
            case DOWN:
                offy = -0.5;
                break;
        }

        offx *= 0.999;
        offz *= 0.999;

        double deltaX = pf.getPos().getX() + 0.5 + offx - mc.thePlayer.posX,
                deltaY = pf.getPos().getY() + 0.5 + offy - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight()),
                deltaZ = pf.getPos().getZ() + 0.5 + offz - mc.thePlayer.posZ,
                distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));


        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ)),
                pitch = (float) -Math.toDegrees(Math.atan(deltaY / distance));

        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if (deltaX < 0 && deltaZ < 0) {
            yaw = (float) (90 + v);
        } else if (deltaX > 0 && deltaZ < 0) {
            yaw = (float) (-90 + v);
        }
        return new float[]{yaw, pitch};
    }


    public static CompleteRotationData getRTS(PosFace face, Vec3 eyePos){
        float[] rtbb = getRotationToBlockDirectWithBestPos(face);
        if(rtbb != null){
            MovingObjectPosition position = getMovingObject(rtbb[0], rtbb[1], face.getPos(), face.getFacing(),eyePos);
            if (position != null) {
                return new CompleteRotationData(position, new float[]{rtbb[0], rtbb[1], 1});
            }
        }
        return null;
    }
    public static CompleteRotationData getRTSNS(PosFace face, Vec3 eyePos){
        float[] rtbb = getRotationToBlockDirect(face);

        if(rtbb != null){
            MovingObjectPosition position = getMovingObjectNOSTRICT(rtbb[0], rtbb[1], face.getPos(),eyePos);
            if (position != null) {
                return new CompleteRotationData(position, new float[]{rtbb[0], rtbb[1], 1});
            }
        }
        return null;
    }

    public float[] getPitchFromYaw(PosFace posFace, float yaw, Vec3 eyePos) {
        float mYaw = 100;
        for (float p = 45; p < 90; p += 1) {
            if (isLookingAtBlock(yaw, p, posFace.getPos(), posFace.getFacing(), eyePos)) {
                mYaw = p;
            }
        }
        if(mYaw == 100){
            return null;
        }
        return new float[]{yaw, mYaw};
    }

    public static CompleteRotationData  getRTS2(PosFace face, float lastYaw, float lastPitch, Vec3 eyePos, int defyReality){
        int def = (int) (Ambient.getInstance().getRotationComponent().getRotation()[0] - 180);

        Vec3 last = new Vec3(lastYaw + 360, lastPitch, 0);
        float dstV = 694200;
        CompleteRotationData cRot = null;

        for(int i = def; i < def + 360; i+= defyReality){
            float[] rotations = getPitchFromYaw(face, i,eyePos);
            if(rotations != null){
                MovingObjectPosition position = getMovingObject(rotations[0], rotations[1], face.getPos(), face.getFacing(),eyePos);
                if (position != null) {
                    Vec3 closest = new Vec3(rotations[0] + 360, rotations[1], 0);
                    float dstGG = (float) last.distanceTo(closest);
                    if(dstGG < dstV){
                        dstV = dstGG;
                        cRot = new CompleteRotationData(position, new float[]{rotations[0], rotations[1], 1});
                    }
                }
            }
        }
        return cRot;
    }

    public static CompleteRotationData getRTS3(PosFace face, float lastYaw, float lastPitch, Vec3 eyePos, int defyReality){
        float[] rtbb = getRotationToBlockEdge(face);
        if(rtbb != null){
            MovingObjectPosition position = getMovingObject(MathHelper.wrapAngleTo180_float(rtbb[0]), rtbb[1], face.getPos(), face.getFacing(),eyePos);
            if(position != null){


                return new CompleteRotationData(position, new float[]{rtbb[0], rtbb[1], 1});
            }

        }
        return null;
    }


    public CompleteRotationData getRotationsFromYaw(PosFace posFace, float yaw, Vec3 eyePos) {
        for (float p = 23; p < 90; p += 1) {
            MovingObjectPosition position = getMovingObject(yaw, p, posFace.getPos(), posFace.getFacing(),eyePos);
            if (position != null) {
                return new CompleteRotationData(position, new float[]{yaw, p, 1});
            }
        }

        return null;
    }
    public CompleteRotationData getRotationsFromYawNS(PosFace posFace, float yaw, Vec3 eyePos) {
        for (float p = 23; p < 90; p += 1) {
            MovingObjectPosition position = getMovingObjectNOSTRICT(yaw, p, posFace.getPos(),eyePos);
            if (position != null) {
                return new CompleteRotationData(position, new float[]{yaw, p, 1});
            }
        }

        return null;
    }

    public boolean isLookingAtBlock(float yaw, float pitch, BlockPos pos, EnumFacing facing, Vec3 eyePos) {
        MovingObjectPosition m = Minecraft.getMinecraft().thePlayer.rayTraceCustomEye(eyePos,6, yaw, pitch);
        if (m == null) {
            return false;
        }

        final Vec3 hitVec = m.hitVec;
        if (hitVec == null) {
            return false;
        }
        return m.getBlockPos().equals(pos) && m.sideHit == facing;

    }

    public MovingObjectPosition getMovingObject(float yaw, float pitch, BlockPos pos, EnumFacing facing, Vec3 eyePos) {
        MovingObjectPosition m = Minecraft.getMinecraft().thePlayer.rayTraceCustomEye(eyePos,6, yaw, pitch);
        if (m == null) {
            return null;
        }
        final Vec3 hitVec = m.hitVec;
        if (hitVec == null) {
            return null;
        }
        if (!(m.getBlockPos().equals(pos) && m.sideHit == facing)) {
            return null;
        }
        return m;
    }
    public MovingObjectPosition getMovingObjectNOSTRICT(float yaw, float pitch, BlockPos pos, Vec3 eyePos) {
        MovingObjectPosition m = Minecraft.getMinecraft().thePlayer.rayTraceCustomEye(eyePos,6, yaw, pitch);
        if (m == null) {
            return null;
        }
        final Vec3 hitVec = m.hitVec;
        if (hitVec == null) {
            return null;
        }
        if (!(m.getBlockPos().equals(pos))) {
            return null;
        }
        return m;
    }


    public MovingObjectPosition getMovingObjectRCU(float yaw, float pitch, BlockPos pos, EnumFacing facing) {
        for(float i = 1f; i > 0f; i -= 0.1f){

            Vec3 vec3 = new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + mc.thePlayer.getEyeHeight(), mc.thePlayer.lastTickPosZ);
            Vec3 vec4 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);


            Vec3 finalEyePos = new Vec3(vec3.xCoord + mc.thePlayer.motionX * i,
                    vec3.yCoord + mc.thePlayer.motionY * i,
                    vec3.zCoord + mc.thePlayer.motionZ * i);




            MovingObjectPosition m = Minecraft.getMinecraft().thePlayer.rayTraceCustomEye(finalEyePos,6, yaw, pitch);
            if (m == null) {
                continue;
            }
            final Vec3 hitVec = m.hitVec;
            if (hitVec == null) {
                continue;
            }
            if (!(m.getBlockPos().equals(pos))) {
                continue;
            }


            return m;
        }
        return null;



    }

    public MovingObjectPosition getMovingObjectFutureTick(float yaw, float pitch, BlockPos pos, EnumFacing facing) {

        Vec3 vec3 = new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + mc.thePlayer.getEyeHeight(), mc.thePlayer.lastTickPosZ);
        Vec3 vec4 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);


        Vec3 finalEyePos = new Vec3(vec4.xCoord + mc.thePlayer.motionX,
                vec4.yCoord +  mc.thePlayer.motionY,
                vec4.zCoord +  mc.thePlayer.motionZ);


        ChatUtil.display(vec4.distanceTo(finalEyePos));



        MovingObjectPosition m = Minecraft.getMinecraft().thePlayer.rayTraceCustomEye(finalEyePos,6, yaw, pitch);
        if (m == null) {
            return null;
        }
        final Vec3 hitVec = m.hitVec;
        if (hitVec == null) {
            return null;
        }
        if (!(m.getBlockPos().equals(pos))) {
            return null;
        }

        return m;


    }


    public BlockPos getNearestBlock(int distance){
        for (int x = -distance; x < distance; x++) {
            for (int z = -distance; z < distance; z++) {
                for (int y = -distance; y <= -1; y++) {
                    BlockPos prevPos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);

                    if(!(mc.theWorld.getBlockState(prevPos).getBlock() instanceof BlockAir)){
                        return prevPos;
                    }

                }
            }
        }
        return null;
    }

    public BlockPos getNearestPlaceble(int distance) {

        float dist = Integer.MAX_VALUE;
        BlockPos pos = null;
        for (int x = -distance; x < distance; x++) {
            for (int z = -distance; z < distance; z++) {
                for (int y = -distance; y <= -1; y++) {
                    BlockPos prevPos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                    double dst = mc.thePlayer.getDistance(prevPos.getX() + 0.5, prevPos.getY(), prevPos.getZ() + 0.5);
                    if (haveBlockNextToIt(prevPos) && dst < dist) {
                        pos = prevPos;
                        dist = (float) dst;
                    }
                }
            }
        }
        if (pos == null) {
            return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        }
        return pos;
    }
    public BlockPos getNearestPlaceble(int distance, int y) {

        float dist = Integer.MAX_VALUE;
        BlockPos pos = null;
        for (int x = -distance; x < distance; x++) {
            for (int z = -distance; z < distance; z++) {
                BlockPos prevPos = new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z);
                double dst = mc.thePlayer.getDistance(prevPos.getX() + 0.5, prevPos.getY(), prevPos.getZ() + 0.5);
                if (haveBlockNextToIt(prevPos) && dst < dist) {
                    pos = prevPos;
                    dist = (float) dst;
                }

            }
        }
        if (pos == null) {
            return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        }
        return pos;
    }

    public boolean haveBlockNextToIt(BlockPos pos) {
        if (!(mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() instanceof BlockAir)) {
            return true;
        }
        if (!(mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() instanceof BlockAir)) {
            return true;
        }
        if (!(mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() instanceof BlockAir)) {
            return true;
        }
        if (!(mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() instanceof BlockAir)) {
            return true;
        }
        return !(mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock() instanceof BlockAir);

    }

    public static float getBlockHardness(final Block block, final ItemStack itemStack, final boolean b) {
        final float getBlockHardness = block.getBlockHardness(mc.theWorld, null);
        if (getBlockHardness < 0.0f) {
            return 0.0f;
        }
        return (block.getMaterial().isToolNotRequired() || (itemStack != null && itemStack.canHarvestBlock(block))) ? (getToolDigEfficiency(itemStack, block, b) / getBlockHardness / 30.0f) : (getToolDigEfficiency(itemStack, block, b) / getBlockHardness / 100.0f);
    }

    public static float getToolDigEfficiency(final ItemStack itemStack, final Block block, final boolean b) {
        float n = (itemStack == null) ? 1.0f : itemStack.getItem().getStrVsBlock(itemStack, block);
        if (n > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0 && itemStack != null) {
                n += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            n *= 1.0f + (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (!b) {
            if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
                float n2;
                switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                    case 0: {
                        n2 = 0.3f;
                        break;
                    }
                    case 1: {
                        n2 = 0.09f;
                        break;
                    }
                    case 2: {
                        n2 = 0.0027f;
                        break;
                    }
                    default: {
                        n2 = 8.1E-4f;
                        break;
                    }
                }
                n *= n2;
            }
            if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
                n /= 5.0f;
            }
        }
        return n;
    }
}
