package fr.ambient.module.impl.player.scaffold.tower;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;

import fr.ambient.util.player.MoveUtil;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

public class WatchdogFastTower2 extends ModuleMode {

    public WatchdogFastTower2(String modeName, Module module) {
        super(modeName, module);
    }

    private boolean canTower = false;
    private boolean Tower = false;
    private int tickCounter;

    @Override
    public void onEnable() {
        canTower = false;
        Tower = false;
        tickCounter = 0;
    }

    @Override
    public void onDisable() {
        Tower = false;
    }

    @SubscribeEvent
    public void onMove(MovementEvent event) {
        if (conditions()) {
            MoveUtil.strafe(MoveUtil.speed());
            switch (mc.thePlayer.airTicks) {
                case 0 -> {
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        double multiplier = 0.98;
                        mc.thePlayer.motionX *= multiplier;
                        mc.thePlayer.motionZ *= multiplier;
                        tickCounter++;
                        event.setY(0.42f);
                        mc.thePlayer.motionY = event.getY() - 0.005;
                    }
                }
                case 1 -> {
                    if (tickCounter > (MoveUtil.isDiag() ? 4 : 6)) {
                        mc.thePlayer.airTicks = 3;
                        tickCounter = 0;
                        return;
                    }
                }
                case 2 -> {
                    mc.thePlayer.airTicks = -1;
                    event.setY(1 - mc.thePlayer.posY % 1);
                    mc.thePlayer.motionY = event.getY() - 0.005;
                }
                case 6 -> mc.thePlayer.motionY = -0.078400001525879;
                case 14 -> mc.thePlayer.motionY = -0.985;
            }
        } else {
            if (!canTower && mc.thePlayer.onGround) canTower = true;
            else if (!mc.thePlayer.onGround) canTower = false;
        }
    }

    private boolean conditions() {
        return mc.gameSettings.keyBindJump.isKeyDown()
                && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() instanceof BlockStairs)
                && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() instanceof BlockSlab) && canTower
                && MoveUtil.moving() && !mc.thePlayer.isCollidedHorizontally;
    }
}
