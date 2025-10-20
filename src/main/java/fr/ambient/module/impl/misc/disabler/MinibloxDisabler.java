package fr.ambient.module.impl.misc.disabler;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.PushOutOfBlockEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;


@Getter
public class MinibloxDisabler extends ModuleMode {

    public MinibloxDisabler(String modeName, Module module) {
        super(modeName, module);
    }

    private double moveSpeed;
    private boolean boost;
    private static final int SEARCH_RADIUS = 500;
    private int lastTeleportTick;


    public void onDisable() {
        MoveUtil.strafe(0);
    }

    public void onEnable() {
        BlockPos nearestBlockPos = findNearestClipBlockOnSameY(SEARCH_RADIUS);

        if (nearestBlockPos != null) {
            boost = true;
            lastTeleportTick = mc.thePlayer.ticksExisted;

            double newX = nearestBlockPos.getX(), newY = nearestBlockPos.getY(), newZ = nearestBlockPos.getZ();

            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(newX + .5, newY, newZ + .5, mc.thePlayer.onGround));
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + mc.thePlayer.motionX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ + mc.thePlayer.motionZ, mc.thePlayer.onGround));
        }
        moveSpeed = 0;
    }

    @SubscribeEvent
    private void onPush(PushOutOfBlockEvent e) {
        e.setCancelled(true);
    }

    @SubscribeEvent
    private void onPreMotion(PreMotionEvent e) {
        if (boost && mc.thePlayer.ticksExisted - lastTeleportTick >= 20) {
            boost = false;
        }

        final double rotation = Math.toRadians(mc.thePlayer.rotationYaw);
        final double x = Math.sin(rotation);
        final double z = Math.cos(rotation);

        if (mc.thePlayer.capabilities.allowFlying) {
            lastTeleportTick = mc.thePlayer.ticksExisted;
        }

        if (mc.thePlayer.ticksExisted % 20 == 0 && !mc.thePlayer.capabilities.allowFlying) {
            BlockPos nearestBlockPos = findNearestClipBlockOnSameY(SEARCH_RADIUS);

            if (nearestBlockPos != null) {
                boost = true;
                lastTeleportTick = mc.thePlayer.ticksExisted;

                double newX = nearestBlockPos.getX(), newY = nearestBlockPos.getY(), newZ = nearestBlockPos.getZ();

                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(newX + .5, newY, newZ + .5, mc.thePlayer.onGround));
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + mc.thePlayer.motionX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ + mc.thePlayer.motionZ, mc.thePlayer.onGround));
            } else {
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(x * 9.5, mc.thePlayer.posY, z * 9.5, mc.thePlayer.onGround));
            }
        }
    }

    @SubscribeEvent
    private void onPacketRecieve(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook s08PacketPlayerPosLook && !mc.thePlayer.capabilities.allowFlying) {
            event.setCancelled(true);
            PacketUtil.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(s08PacketPlayerPosLook.x, s08PacketPlayerPosLook.y, s08PacketPlayerPosLook.z, s08PacketPlayerPosLook.yaw, s08PacketPlayerPosLook.pitch, false));
        }
    }

    private BlockPos findNearestClipBlockOnSameY(int radius) {
        double playerX = mc.thePlayer.posX, playerY = mc.thePlayer.posY, playerZ = mc.thePlayer.posZ;
        int yLevel = MathHelper.floor_double(playerY);

        BlockPos bestBlockPos = null;
        double closestDistance = Double.MAX_VALUE;

        for (int xOff = -radius; xOff <= radius; xOff++) {
            for (int zOff = -radius; zOff <= radius; zOff++) {
                BlockPos pos = new BlockPos(MathHelper.floor_double(playerX) + xOff, yLevel, MathHelper.floor_double(playerZ) + zOff);
                Block block = mc.theWorld.getBlockState(pos).getBlock();
                Material material = block.getMaterial();

                if (material.isSolid() && block.isFullBlock() && !(block instanceof BlockLeaves) && !(block instanceof BlockSnow)) {
                    double dist = mc.thePlayer.getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        bestBlockPos = pos;
                    }
                }
            }
        }
        return bestBlockPos;
    }
}