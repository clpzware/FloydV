package fr.ambient.module.impl.movement.speed;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.block.*;
import net.minecraft.util.BlockPos;

public class NcpSpeed extends ModuleMode {
    public NcpSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1f;
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        if (mc.thePlayer.onGround & MoveUtil.moving() && !isNearnonfullblock()) {
            mc.thePlayer.motionY = 0.42;
            MoveUtil.strafe(0.48f, 0.58f, 0.68f);
        }
        MoveUtil.strafe(MoveUtil.speed());
        if (mc.thePlayer.airTicks == 4) {
            mc.thePlayer.motionY = -0.0980000019073;
        }
        if (((Speed) this.getSuperModule()).timerboost.getValue() && !isNearnonfullblock2()) {
            switch (mc.thePlayer.airTicks) {
                case 0 -> mc.timer.timerSpeed = 1.5f;
                case 1 -> mc.timer.timerSpeed = 1.9f;
                case 3 -> mc.timer.timerSpeed = 1.08f;
                default -> mc.timer.timerSpeed = 1;
            }
        }
    }

    public boolean isNearnonfullblock2() {
        for (float y = -1; y < 1; y++) {
            if (isNonFullBlock2(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ))) {
                return true;
            }
        }
        return false;
    }

    public boolean isNonFullBlock2(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockWeb || block instanceof BlockSlime || block instanceof BlockIce || block instanceof BlockSoulSand;
    }







    public boolean isNearnonfullblock() {
        for (float y = -1; y < 1; y++) {
            if (isNonFullBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ))) {
                return true;
            }
        }
        return false;
    }

    public boolean isNonFullBlock(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockWeb || block instanceof BlockSlime;
    }



    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        event.setJumping(false);
    }
}